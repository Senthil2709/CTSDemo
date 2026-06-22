export interface AuditAskRequest {
  question: string;
}

export interface AuditToolCall {
  tool: string;
  input: Record<string, unknown>;
  output: unknown;
}

export interface AuditAskResponse {
  answer: string;
  toolCalls?: AuditToolCall[];
}
