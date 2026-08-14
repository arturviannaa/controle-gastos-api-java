/**
 * Página de cadastro de transações: formulário de criação e listagem.
 * A regra de que menores de idade só podem cadastrar despesas é
 * garantida pelo back-end; aqui o formulário também desabilita a
 * opção "Receita" quando a pessoa selecionada é menor, para dar
 * retorno imediato ao usuário.
 */
import { useEffect, useState, type FormEvent } from 'react';
import { createTransaction, listPeople, listTransactions } from '../api';
import { formatCurrency } from '../format';
import type { Person, Transaction, TransactionType } from '../types';

const ADULT_AGE = 18;

export function TransactionsPage() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [people, setPeople] = useState<Person[]>([]);
  const [description, setDescription] = useState('');
  const [amount, setAmount] = useState('');
  const [type, setType] = useState<TransactionType>('Expense');
  const [personId, setPersonId] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  /** Carrega transações e pessoas (para o seletor do formulário). */
  async function loadData() {
    try {
      const [transactionList, peopleList] = await Promise.all([
        listTransactions(),
        listPeople(),
      ]);
      setTransactions(transactionList);
      setPeople(peopleList);
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  const selectedPerson = people.find((p) => p.id === Number(personId));
  const isMinorSelected = selectedPerson !== undefined && selectedPerson.age < ADULT_AGE;

  /** Envia o formulário de criação e atualiza a listagem. */
  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    try {
      await createTransaction({
        description: description.trim(),
        amount: Number(amount),
        type,
        personId: Number(personId),
      });
      setDescription('');
      setAmount('');
      await loadData();
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <section>
      <div className="card">
        <h2>Nova transação</h2>
        {people.length === 0 && !isLoading ? (
          <p className="empty-message">
            Cadastre uma pessoa antes de registrar transações.
          </p>
        ) : (
          <form className="form-row" onSubmit={handleSubmit}>
            <div className="form-field">
              <label htmlFor="tx-description">Descrição</label>
              <input
                id="tx-description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Ex.: Supermercado"
                required
              />
            </div>
            <div className="form-field">
              <label htmlFor="tx-amount">Valor (R$)</label>
              <input
                id="tx-amount"
                type="number"
                min={0.01}
                step={0.01}
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="Ex.: 100,00"
                required
              />
            </div>
            <div className="form-field">
              <label htmlFor="tx-person">Pessoa</label>
              <select
                id="tx-person"
                value={personId}
                onChange={(e) => {
                  setPersonId(e.target.value);
                  // Se a pessoa selecionada for menor de idade, força o
                  // tipo para despesa (receita não é permitida).
                  const person = people.find((p) => p.id === Number(e.target.value));
                  if (person && person.age < ADULT_AGE) {
                    setType('Expense');
                  }
                }}
                required
              >
                <option value="" disabled>
                  Selecione…
                </option>
                {people.map((person) => (
                  <option key={person.id} value={person.id}>
                    {person.name} ({person.age} anos)
                  </option>
                ))}
              </select>
            </div>
            <div className="form-field">
              <label htmlFor="tx-type">Tipo</label>
              <select
                id="tx-type"
                value={type}
                onChange={(e) => setType(e.target.value as TransactionType)}
              >
                <option value="Expense">Despesa</option>
                <option value="Income" disabled={isMinorSelected}>
                  Receita{isMinorSelected ? ' (indisponível para menores)' : ''}
                </option>
              </select>
            </div>
            <button className="primary" type="submit">
              Cadastrar
            </button>
          </form>
        )}
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="card">
        <h2>Transações cadastradas</h2>
        {isLoading ? (
          <p className="empty-message">Carregando…</p>
        ) : transactions.length === 0 ? (
          <p className="empty-message">Nenhuma transação cadastrada ainda.</p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Descrição</th>
                  <th>Pessoa</th>
                  <th>Tipo</th>
                  <th className="number">Valor</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((tx) => (
                  <tr key={tx.id}>
                    <td>{tx.id}</td>
                    <td>{tx.description}</td>
                    <td>{tx.personName}</td>
                    <td>
                      <span className={`badge ${tx.type === 'Income' ? 'income' : 'expense'}`}>
                        {tx.type === 'Income' ? 'Receita' : 'Despesa'}
                      </span>
                    </td>
                    <td className={`number ${tx.type === 'Income' ? 'income' : 'expense'}`}>
                      {formatCurrency(tx.amount)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}
