/**
 * Página de cadastro de pessoas: formulário de criação, listagem e
 * botão de deleção (que também apaga as transações da pessoa,
 * conforme regra implementada no back-end).
 */
import { useEffect, useState, type FormEvent } from 'react';
import { createPerson, deletePerson, listPeople } from '../api';
import type { Person } from '../types';

export function PeoplePage() {
  const [people, setPeople] = useState<Person[]>([]);
  const [name, setName] = useState('');
  const [age, setAge] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  /** Recarrega a listagem a partir da API. */
  async function loadPeople() {
    try {
      setPeople(await listPeople());
      setError(null);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadPeople();
  }, []);

  /** Envia o formulário de criação e atualiza a lista. */
  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    try {
      await createPerson(name.trim(), Number(age));
      // Limpa o formulário apenas em caso de sucesso, para o usuário
      // não perder o que digitou quando houver erro de validação.
      setName('');
      setAge('');
      await loadPeople();
    } catch (err) {
      setError((err as Error).message);
    }
  }

  /** Deleta a pessoa após confirmação do usuário. */
  async function handleDelete(person: Person) {
    const confirmed = window.confirm(
      `Deletar "${person.name}"? Todas as transações dessa pessoa também serão apagadas.`
    );
    if (!confirmed) {
      return;
    }
    try {
      await deletePerson(person.id);
      await loadPeople();
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <section>
      <div className="card">
        <h2>Nova pessoa</h2>
        <form className="form-row" onSubmit={handleSubmit}>
          <div className="form-field">
            <label htmlFor="person-name">Nome</label>
            <input
              id="person-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Ex.: Maria"
              required
            />
          </div>
          <div className="form-field">
            <label htmlFor="person-age">Idade</label>
            <input
              id="person-age"
              type="number"
              min={0}
              max={150}
              value={age}
              onChange={(e) => setAge(e.target.value)}
              placeholder="Ex.: 30"
              required
            />
          </div>
          <button className="primary" type="submit">
            Cadastrar
          </button>
        </form>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="card">
        <h2>Pessoas cadastradas</h2>
        {isLoading ? (
          <p className="empty-message">Carregando…</p>
        ) : people.length === 0 ? (
          <p className="empty-message">Nenhuma pessoa cadastrada ainda.</p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nome</th>
                  <th className="number">Idade</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {people.map((person) => (
                  <tr key={person.id}>
                    <td>{person.id}</td>
                    <td>{person.name}</td>
                    <td className="number">{person.age}</td>
                    <td className="number">
                      <button className="danger" onClick={() => handleDelete(person)}>
                        Deletar
                      </button>
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
