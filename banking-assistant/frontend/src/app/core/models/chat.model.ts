export interface ChatRequest {
  message: string;
  sessionId?: string;
}

export interface ChatResponse {
  sessionId: string;
  reply: string;
  intentsHandled: string[];
  blocked: boolean;
}

export interface ChatMessageDto {
  sender: 'USER' | 'ASSISTANT' | 'SYSTEM';
  intent?: string;
  content: string;
  createdAt: string;
}

export interface ChatHistoryResponse {
  sessionId: string;
  status: string;
  messages: ChatMessageDto[];
}

/** Local view-model used by the chat widget while a message is in flight. */
export interface ChatBubble {
  sender: 'USER' | 'ASSISTANT' | 'SYSTEM';
  content: string;
  blocked?: boolean;
  pending?: boolean;
}
