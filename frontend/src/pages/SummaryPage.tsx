/**
 * Página da consulta de totais: lista todas as pessoas com o total de
 * receitas, despesas e saldo de cada uma, e exibe ao final o total
 * geral. Os cálculos são feitos pelo back-end; aqui apenas exibimos.
 */
import { useEffect, useState } from 'react';
import { getSummary } from '../api';
import { formatCurrency } from '../format';
import type { Summary } from '../types';

export function SummaryPage() {
  const [summary, setSummary] = useState<Summary | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getSummary()
      .then(setSummary)
      .catch((err) => setError((err as Error).message));
  }, []);

  /** Aplica cor verde/vermelha conforme o sinal do saldo. */
  function balanceClass(balance: number): string {
    if (balance > 0) return 'income';
    if (balance < 0) return 'expense';
    return '';
  }

  if (error) {
    return <div className="error-message">{error}</div>;
  }

  if (!summary) {
    return <p className="empty-message">Carregando…</p>;
  }

  return (
    <section className="card">
      <h2>Totais por pessoa</h2>
      {summary.people.length === 0 ? (
        <p className="empty-message">Nenhuma pessoa cadastrada ainda.</p>
      ) : (
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Pessoa</th>
                <th className="number">Receitas</th>
                <th className="number">Despesas</th>
                <th className="number">Saldo</th>
              </tr>
            </thead>
            <tbody>
              {summary.people.map((person) => (
                <tr key={person.personId}>
                  <td>{person.personName}</td>
                  <td className="number income">{formatCurrency(person.totalIncome)}</td>
                  <td className="number expense">{formatCurrency(person.totalExpense)}</td>
                  <td className={`number ${balanceClass(person.balance)}`}>
                    {formatCurrency(person.balance)}
                  </td>
                </tr>
              ))}
              {/* Linha final com o total geral de todas as pessoas. */}
              <tr className="grand-total">
                <td>Total geral</td>
                <td className="number income">{formatCurrency(summary.grandTotalIncome)}</td>
                <td className="number expense">{formatCurrency(summary.grandTotalExpense)}</td>
                <td className={`number ${balanceClass(summary.grandTotalBalance)}`}>
                  {formatCurrency(summary.grandTotalBalance)}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
