/**
 * Cliente HTTP da API. Centraliza as chamadas ao back-end e o
 * tratamento de erros, para que os componentes lidem apenas com
 * dados prontos ou mensagens de erro amigáveis.
 */
import type { Person, Summary, Transaction, TransactionType } from './types';

// endereco da API: VITE_API_URL no build, com o docker compose local como padrao
const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8092/api';

/**
 * Executa uma requisição e converte respostas de erro da API em
 * exceções com mensagem legível para o usuário.
 */
async function request<T>(path: string, options?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
  } catch {
    // Falha de rede: provavelmente a API não está rodando.
    throw new Error('Não foi possível conectar à API. Verifique se o back-end está em execução.');
  }

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response));
  }

  // 204 No Content (ex.: deleção) não possui corpo para desserializar.
  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

/**
 * Extrai a mensagem de erro do corpo da resposta. A API retorna
 * `{ message }` nos erros de regra de negócio e o formato
 * ValidationProblemDetails (`{ errors }`) nos erros de validação.
 */
async function extractErrorMessage(response: Response): Promise<string> {
  try {
    const body = await response.json();
    if (typeof body?.message === 'string') {
      return body.message;
    }
    if (body?.errors && typeof body.errors === 'object') {
      const messages = Object.values(body.errors).flat();
      if (messages.length > 0) {
        return messages.join(' ');
      }
    }
  } catch {
    // Corpo não era JSON: usa a mensagem genérica abaixo.
  }
  return `Erro inesperado na comunicação com a API (HTTP ${response.status}).`;
}

// ---- Pessoas ----

export function listPeople(): Promise<Person[]> {
  return request<Person[]>('/people');
}

export function createPerson(name: string, age: number): Promise<Person> {
  return request<Person>('/people', {
    method: 'POST',
    body: JSON.stringify({ name, age }),
  });
}

export function deletePerson(id: number): Promise<void> {
  return request<void>(`/people/${id}`, { method: 'DELETE' });
}

// ---- Transações ----

export function listTransactions(): Promise<Transaction[]> {
  return request<Transaction[]>('/transactions');
}

export function createTransaction(input: {
  description: string;
  amount: number;
  type: TransactionType;
  personId: number;
}): Promise<Transaction> {
  return request<Transaction>('/transactions', {
    method: 'POST',
    body: JSON.stringify(input),
  });
}

// ---- Totais ----

export function getSummary(): Promise<Summary> {
  return request<Summary>('/summary');
}
