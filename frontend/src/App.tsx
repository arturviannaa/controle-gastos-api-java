/**
 * Componente raiz: título da aplicação e navegação por abas entre as
 * três funcionalidades (pessoas, transações e totais). A troca de aba
 * remonta a página, o que recarrega os dados da API — assim os totais
 * sempre refletem os cadastros mais recentes.
 */
import { useState } from 'react';
import { PeoplePage } from './pages/PeoplePage';
import { SummaryPage } from './pages/SummaryPage';
import { TransactionsPage } from './pages/TransactionsPage';

type Tab = 'people' | 'transactions' | 'summary';

const TABS: { id: Tab; label: string }[] = [
  { id: 'people', label: 'Pessoas' },
  { id: 'transactions', label: 'Transações' },
  { id: 'summary', label: 'Totais' },
];

function App() {
  const [activeTab, setActiveTab] = useState<Tab>('people');

  return (
    <main>
      <h1>Controle de Gastos Residenciais</h1>

      <nav className="tabs">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            className={activeTab === tab.id ? 'active' : ''}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      {activeTab === 'people' && <PeoplePage />}
      {activeTab === 'transactions' && <TransactionsPage />}
      {activeTab === 'summary' && <SummaryPage />}
    </main>
  );
}

export default App;
