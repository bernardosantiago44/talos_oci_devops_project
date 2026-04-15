import React, { useState, useEffect } from 'react';
import NewItem from './NewItem';
import Dashboard from './Dashboard';
import API_LIST from './API';
import DeleteIcon from '@mui/icons-material/Delete';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import FormatListBulletedIcon from '@mui/icons-material/FormatListBulleted';
import BarChartIcon from '@mui/icons-material/BarChart';
import { CircularProgress } from '@mui/material';
import Moment from 'react-moment';

const CARD_COLORS = ['#7C3AED', '#F59E0B', '#14B8A6', '#EC4899', '#3B82F6', '#EF4444'];

function App() {
  const [activeTab, setActiveTab] = useState('tasks');
  const [isLoading] = useState(false);
  const [isInserting, setInserting] = useState(false);
  const [items, setItems] = useState([]);
  const [, setError] = useState();

  function deleteItem(deleteId) {
    fetch(API_LIST + "/" + deleteId, { method: 'DELETE' })
      .then(response => {
        if (response.ok) return response;
        throw new Error('Something went wrong ...');
      })
      .then(
        () => { setItems(prev => prev.filter(item => item.id !== deleteId)); },
        (err) => { setError(err); }
      );
  }

  function toggleDone(event, id, description, done) {
    event.preventDefault();
    modifyItem(id, description, done).then(
      () => { reloadOneItem(id); },
      (err) => { setError(err); }
    );
  }

  function reloadOneItem(id) {
    fetch(API_LIST + "/" + id)
      .then(response => {
        if (response.ok) return response.json();
        throw new Error('Something went wrong ...');
      })
      .then(
        (result) => {
          setItems(prev => prev.map(x =>
            x.id === id ? { ...x, description: result.description, done: result.done } : x
          ));
        },
        (err) => { setError(err); }
      );
  }

  function modifyItem(id, description, done) {
    return fetch(API_LIST + "/" + id, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ description, done }),
    }).then(response => {
      if (response.ok) return response;
      throw new Error('Something went wrong ...');
    });
  }

  useEffect(() => {
    setItems([
      { id: 1, description: 'Design new dashboard layout', createdAt: '2026-04-14T09:00:00', done: false },
      { id: 2, description: 'Fix login bug on mobile', createdAt: '2026-04-14T10:30:00', done: false },
      { id: 3, description: 'Write unit tests for API', createdAt: '2026-04-13T15:00:00', done: false },
      { id: 4, description: 'Deploy to staging environment', createdAt: '2026-04-13T11:00:00', done: true },
      { id: 5, description: 'Review pull request #42', createdAt: '2026-04-12T08:00:00', done: true },
    ]);
  }, []);

  function addItem(text) {
    setInserting(true);
    fetch(API_LIST, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ description: text }),
    })
      .then(response => {
        if (response.ok) return response;
        throw new Error('Something went wrong ...');
      })
      .then(
        (result) => {
          const id = result.headers.get('location');
          setItems(prev => [{ id, description: text }, ...prev]);
          setInserting(false);
        },
        (err) => { setInserting(false); setError(err); }
      );
  }

  const todoItems = items.filter(item => !item.done);
  const doneItems = items.filter(item => item.done);
  const donePercent = items.length > 0 ? Math.round((doneItems.length / items.length) * 100) : 0;

  return (
    <div className="app-wrapper">
      <div className="layout">

        {/* Left panel — header + stats */}
        <div className="left-panel">
          <header className="app-header">
            <div className="header-logo">
              <TaskAltIcon />
            </div>
            <h1>My Tasks</h1>
            <p className="subtitle">Stay organized, stay focused</p>
            <div className="stats-row">
              <div className="stat-chip">
                <span className="dot dot-pending" />
                {todoItems.length} pending
              </div>
              <div className="stat-chip">
                <span className="dot dot-done" />
                {doneItems.length} completed
              </div>
            </div>
          </header>

          {items.length > 0 && (
            <div className="progress-wrap">
              <div className="progress-label">
                <span>{doneItems.length} of {items.length} tasks completed</span>
                <span className="pct">{donePercent}%</span>
              </div>
              <div className="progress-track">
                <div className="progress-fill" style={{ width: `${donePercent}%` }} />
              </div>
            </div>
          )}
        </div>

        {/* Right panel — tasks */}
        <div className="right-panel">
          <div className="tabs">
            <button
              className={`tab-btn ${activeTab === 'tasks' ? 'active' : ''}`}
              onClick={() => setActiveTab('tasks')}
            >
              <FormatListBulletedIcon style={{ fontSize: 16 }} />
              Tasks
            </button>
            <button
              className={`tab-btn ${activeTab === 'analytics' ? 'active' : ''}`}
              onClick={() => setActiveTab('analytics')}
            >
              <BarChartIcon style={{ fontSize: 16 }} />
              Analytics
            </button>
          </div>

          {activeTab === 'analytics' ? (
            <Dashboard />
          ) : (
          <div className="app-body">
            <NewItem addItem={addItem} isInserting={isInserting} />

            {isLoading ? (
              <div className="loading-wrap">
                <CircularProgress size={28} style={{ color: '#7C3AED' }} />
              </div>
            ) : (
              <>
                <section className="tasks-section">
                  <div className="section-header">
                    <h2>To Do</h2>

                    <span className="count-badge">{todoItems.length}</span>
                  </div>

                  {todoItems.length === 0 ? (
                    <div className="empty-state">All caught up — nothing left to do!</div>
                  ) : (
                    todoItems.map((item, i) => (
                      <div
                        key={item.id}
                        className="task-card"
                        style={{ '--card-accent': CARD_COLORS[i % CARD_COLORS.length] }}
                      >
                        <button
                          className="task-checkbox"
                          onClick={(e) => toggleDone(e, item.id, item.description, true)}
                          title="Mark as done"
                        />
                        <div className="task-body">
                          <span className="task-description">{item.description}</span>
                          {item.createdAt && (
                            <span className="task-date">
                              <Moment format="MMM D, h:mm a">{item.createdAt}</Moment>
                            </span>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </section>

                {doneItems.length > 0 && (
                  <section className="tasks-section">
                    <div className="section-header">
                      <h2>Completed</h2>
                      <span className="count-badge">{doneItems.length}</span>
                    </div>
                    {doneItems.map((item) => (
                      <div key={item.id} className="task-card is-done">
                        <button
                          className="task-checkbox checked"
                          onClick={(e) => toggleDone(e, item.id, item.description, false)}
                          title="Mark as to do"
                        />
                        <div className="task-body">
                          <span className="task-description">{item.description}</span>
                          {item.createdAt && (
                            <span className="task-date">
                              <Moment format="MMM D, h:mm a">{item.createdAt}</Moment>
                            </span>
                          )}
                        </div>
                        <div className="task-actions">
                          <button
                            className="action-btn delete"
                            onClick={() => deleteItem(item.id)}
                            title="Delete task"
                          >
                            <DeleteIcon style={{ fontSize: 16 }} />
                          </button>
                        </div>
                      </div>
                    ))}
                  </section>
                )}
              </>
            )}
          </div>
          )}
        </div>

      </div>
    </div>
  );
}

export default App;
