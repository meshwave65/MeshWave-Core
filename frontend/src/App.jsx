// frontend/src/App.jsx (v1.2 - com Cache Busting)

import { useState, useEffect } from 'react';
import './App.css';

function App() {
  // ... (seus outros estados useState permanecem os mesmos)
  const [segments, setSegments] = useState([]);
  const [phases, setPhases] = useState([]);
  const [selectedSegment, setSelectedSegment] = useState(null);
  const [apiUrl, setApiUrl] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // --- A MUDANÇA ESTÁ AQUI ---
    // Adicionamos um parâmetro inútil com a data atual para burlar o cache da CDN
    const cacheBuster = `?v=${new Date().getTime()}`;
    const configUrl = `https://raw.githubusercontent.com/meshwave65/MeshWave-Core/main/config.json${cacheBuster}`;
    // --- FIM DA MUDANÇA ---

    console.log(`Buscando configuração de: ${configUrl}` ); // Log para depuração

    fetch(configUrl)
      .then(res => {
        if (!res.ok) throw new Error(`Erro ao buscar config.json: ${res.statusText}`);
        return res.json();
      })
      .then(config => {
        setApiUrl(config.apiUrl);
        console.log(`API URL carregada: ${config.apiUrl}`);
        return fetch(`${config.apiUrl}/api/v1/segments`, { headers: { 'ngrok-skip-browser-warning': 'true' } });
      })
      .then(res => {
        if (!res.ok) throw new Error(`A resposta da rede para /segments não foi ok: ${res.statusText}`);
        return res.json();
      })
      .then(data => {
        setSegments(data);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  // ... (o resto do seu código, como handleSegmentClick, permanece o mesmo)
  const handleSegmentClick = (segment) => {
    setSelectedSegment(segment);
    setPhases([]);
    setLoading(true);

    fetch(`${apiUrl}/api/v1/segments/${segment.id}/phases`, { headers: { 'ngrok-skip-browser-warning': 'true' } })
      .then(res => {
        if (!res.ok) throw new Error(`A resposta da rede para /phases não foi ok: ${res.statusText}`);
        return res.json();
      })
      .then(data => {
        setPhases(data);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  };

  if (error) return <div>Erro ao carregar os dados: {error}</div>;

  return (
    <div className="container">
      <header>
        <h1>Project C3 - Roadmap</h1>
        <p><strong>API Conectada:</strong> {apiUrl}</p>
      </header>
      <hr />
      <main className="main-layout">
        <nav className="sidebar">
          <h2>Segments</h2>
          {loading && segments.length === 0 ? <p>Carregando...</p> : (
            <ul>
              {segments.map(segment => (
                <li key={segment.id} onClick={() => handleSegmentClick(segment)} className={selectedSegment?.id === segment.id ? 'selected' : ''}>
                  {segment.name}
                </li>
              ))}
            </ul>
          )}
        </nav>
        <section className="content">
          {selectedSegment ? (
            <>
              <h2>Fases de: {selectedSegment.name}</h2>
              {loading && phases.length === 0 ? <p>Carregando fases...</p> : (
                <ul>
                  {phases.map(phase => (
                    <li key={phase.id}>{phase.name}</li>
                  ))}
                </ul>
              )}
            </>
          ) : (
            <h2>Selecione um Segmento para ver as Fases</h2>
          )}
        </section>
      </main>
    </div>
  );
}

export default App;

