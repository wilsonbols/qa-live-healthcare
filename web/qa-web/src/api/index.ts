const BASE_URL = '/api/questions';

export interface QuestionDTO {
  id?: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName?: string;
  question: string;
  submitTime?: string;
  status?: 'pending' | 'answered';
  answer?: string | null;
  answerTime?: string | null;
}

export interface CreateQuestionRequest {
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName?: string;
  question: string;
}

export interface AnswerRequest {
  answer: string;
}

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`HTTP ${res.status}: ${text}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

/** 获取问题列表，支持多条件组合筛选 */
export function getQuestions(params?: {
  patientId?: number;
  doctorId?: number;
  status?: string;
  startDate?: string;
  endDate?: string;
}): Promise<QuestionDTO[]> {
  const search = new URLSearchParams();
  if (params?.patientId) search.set('patientId', String(params.patientId));
  if (params?.doctorId) search.set('doctorId', String(params.doctorId));
  if (params?.status) search.set('status', params.status);
  if (params?.startDate) search.set('startDate', params.startDate);
  if (params?.endDate) search.set('endDate', params.endDate);
  const qs = search.toString();
  return request<QuestionDTO[]>(`${BASE_URL}${qs ? '?' + qs : ''}`);
}

/** 根据 ID 获取单个问题 */
export function getQuestionById(id: number): Promise<QuestionDTO> {
  return request<QuestionDTO>(`${BASE_URL}/${id}`);
}

/** 创建问题 */
export function createQuestion(data: CreateQuestionRequest): Promise<QuestionDTO> {
  return request<QuestionDTO>(BASE_URL, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/** 更新问题 */
export function updateQuestion(id: number, data: CreateQuestionRequest): Promise<QuestionDTO> {
  return request<QuestionDTO>(`${BASE_URL}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

/** 回答问题 */
export function answerQuestion(id: number, data: AnswerRequest): Promise<QuestionDTO> {
  return request<QuestionDTO>(`${BASE_URL}/${id}/answer`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

/** 删除问题 */
export function deleteQuestion(id: number): Promise<void> {
  return request<void>(`${BASE_URL}/${id}`, { method: 'DELETE' });
}
