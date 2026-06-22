export interface PolicySourceDocument {
  title: string;
  category: string;
  relevanceScore: number;
}

export interface PolicyAnswerResponse {
  answer: string;
  sources: PolicySourceDocument[];
}
