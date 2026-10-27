import os
import glob
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from dotenv import load_dotenv

from langchain_community.document_loaders import PyPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain.chains import create_history_aware_retriever, create_retrieval_chain
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder, PromptTemplate
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.runnables.history import RunnableWithMessageHistory

load_dotenv() # Lee GOOGLE_API_KEY desde tu archivo .env

app = FastAPI(title="Servicio RAG Prácticas UBB")

RUTA_PDFS = "./documentos_reglamento" # Carpeta con los PDFs base del chatbot
RUTA_INDICE_FAISS = "./faiss_index"
store = {}

def get_session_history(session_id: str):
    if session_id not in store:
        store[session_id] = ChatMessageHistory()
    return store[session_id]

def cargar_o_crear_vectorstore():
    embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")
    
    # Si ya existe el índice en disco, lo carga en milisegundos
    if os.path.exists(RUTA_INDICE_FAISS):
        return FAISS.load_local(RUTA_INDICE_FAISS, embeddings, allow_dangerous_deserialization=True)
    
    os.makedirs(RUTA_PDFS, exist_ok=True)
    documents = []
    for file in glob.glob(f"{RUTA_PDFS}/*.pdf"):
        documents.extend(PyPDFLoader(file).load())
        
    if not documents:
        print(f"⚠️ Pon al menos un PDF en {RUTA_PDFS} y reinicia.")
        return None

    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=1000, chunk_overlap=200, separators=["\n\n", "\n", " ", ""]
    )
    splits = text_splitter.split_documents(documents)
    vectorstore = FAISS.from_documents(splits, embeddings)
    vectorstore.save_local(RUTA_INDICE_FAISS)
    return vectorstore

def inicializar_rag_chain(vectorstore):
    if vectorstore is None:
        return None

    llm = ChatGoogleGenerativeAI(model="gemini-2.5-flash", temperature=0.3)
    retriever = vectorstore.as_retriever(search_kwargs={"k": 5})

    contextualize_q_system_prompt = (
        "Dado el historial del chat y la última pregunta del usuario "
        "la cual podría hacer referencia al contexto del historial, "
        "formula una pregunta independiente que pueda entenderse "
        "sin el historial del chat. NO respondas la pregunta, "
        "simplemente reformúlala si es necesario y de lo contrario devuélvela tal cual."
    )

    qa_system_prompt = (
        "Eres el asistente virtual del Sistema de Prácticas Pedagógicas de la Universidad del Bío-Bío. "
        "Usa los siguientes fragmentos de contexto recuperado para responder la pregunta. "
        "Si no sabes la respuesta, simplemente di que no lo sabes. "
        "Usa un máximo de tres oraciones y mantén la respuesta concisa. "
        "Responde SIEMPRE en español.\n\n"
        "Contexto: {context}"
    )

    history_prompt = ChatPromptTemplate.from_messages([
        ("system", contextualize_q_system_prompt),
        MessagesPlaceholder("chat_history"),
        ("human", "{input}"),
    ])

    qa_prompt = ChatPromptTemplate.from_messages([
        ("system", qa_system_prompt),
        MessagesPlaceholder("chat_history"),
        ("human", "{input}"),
    ])

    document_prompt = PromptTemplate(
        input_variables=["page_content", "source"],
        template="Contenido: {page_content}\nFuente: {source}"
    )

    history_aware_retriever = create_history_aware_retriever(llm, retriever, history_prompt)
    question_answer_chain = create_stuff_documents_chain(llm, qa_prompt, document_prompt=document_prompt)
    rag_chain = create_retrieval_chain(history_aware_retriever, question_answer_chain)

    return RunnableWithMessageHistory(
        rag_chain,
        get_session_history,
        input_messages_key="input",
        history_messages_key="chat_history",
        output_messages_key="answer",
    )

vectorstore = cargar_o_crear_vectorstore()
agente_chain = inicializar_rag_chain(vectorstore)

class ConsultaChat(BaseModel):
    mensaje: str
    session_id: str

@app.post("/chat")
def endpoint_chat(consulta: ConsultaChat):
    if not agente_chain:
        raise HTTPException(status_code=503, detail="El índice de documentos PDF aún no está cargado.")
    try:
        response = agente_chain.invoke(
            {"input": consulta.mensaje},
            config={"configurable": {"session_id": consulta.session_id}}
        )
        return {"respuesta": response["answer"]}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))