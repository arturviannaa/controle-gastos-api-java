/**
 * Tipos compartilhados do front-end, espelhando os DTOs da API.
 */

/** Tipo da transação, serializado como texto pela API. */
export type TransactionType = 'Expense' | 'Income';

export interface Person {
  id: number;
  name: string;
  age: number;
}

export interface Transaction {
  id: number;
  description: string;
  amount: number;
  type: TransactionType;
  personId: number;
  /** Nome da pessoa, incluído pela API para facilitar a listagem. */
  personName: string;
}

/** Totais de uma pessoa: receitas, despesas e saldo. */
export interface PersonSummary {
  personId: number;
  personName: string;
  totalIncome: number;
  totalExpense: number;
  balance: number;
}

/** Consulta de totais completa, incluindo o total geral. */
export interface Summary {
  people: PersonSummary[];
  grandTotalIncome: number;
  grandTotalExpense: number;
  grandTotalBalance: number;
}
