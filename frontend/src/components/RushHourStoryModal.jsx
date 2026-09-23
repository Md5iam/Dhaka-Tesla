import React, { useState } from 'react';
import { api } from '../api/apiService';
import { X, Play, ShieldAlert, CheckCircle2, RotateCcw } from 'lucide-react';

export default function RushHourStoryModal({ isOpen, onClose, onRefreshAll, addToast }) {
  if (!isOpen) return null;

  const [loading, setLoading] = useState(false);
  const [timeline, setTimeline] = useState(null);

  const handleRunSimulation = async () => {
    try {
      setLoading(true);
      const res = await api.runRushHourSimulation();
      setTimeline(res.timeline || []);
      addToast('Rush-Hour simulation completed successfully', 'success');
      await onRefreshAll();
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = async () => {
    try {
      await api.resetSimulation();
      setTimeline(null);
      addToast('System reset to initial state', 'success');
      await onRefreshAll();
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card" style={{ maxWidth: '640px' }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <h2 className="editorial-serif" style={{ fontSize: '24px' }}>
              The Banani Rush-Hour Story
            </h2>
            <p style={{ fontSize: '13px', color: 'var(--ink-secondary)', marginTop: '4px' }}>
              8:41 AM on Banani Road 11 — Nusrat, Rafiq, Shirin, and Jashim's Bullet
            </p>
          </div>
          <button className="modal-close-btn" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <div style={{ display: 'flex', gap: '10px', marginBottom: '24px' }}>
          <button
            type="button"
            className="btn-clay"
            onClick={handleRunSimulation}
            disabled={loading}
          >
            <Play size={14} fill="#FFFFFF" />
            <span>{loading ? 'Simulating Traffic...' : 'Execute Rush-Hour Story'}</span>
          </button>

          <button
            type="button"
            className="btn-outline"
            onClick={handleReset}
            disabled={loading}
          >
            <RotateCcw size={14} />
            <span>Reset Demo</span>
          </button>
        </div>

        {timeline && timeline.length > 0 ? (
          <div>
            {timeline.map((item, idx) => {
              const isError = item.result === 'REJECTED_CAPACITY_EXCEEDED';
              return (
                <div
                  key={idx}
                  className={`timeline-step-card ${isError ? '' : 'active'}`}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      {isError ? (
                        <ShieldAlert size={16} color="#B45309" />
                      ) : (
                        <CheckCircle2 size={16} color="#2E7D32" />
                      )}
                      <span style={{ fontWeight: 600, fontSize: '14px' }}>
                        {item.step}
                      </span>
                    </div>
                    {item.fare && (
                      <span className="badge badge-clay">
                        {Number(item.fare).toFixed(2)} BDT
                      </span>
                    )}
                  </div>

                  {isError ? (
                    <div style={{ marginTop: '8px', fontSize: '12px', color: 'var(--status-amber)' }}>
                      <strong>Capacity Rule Enforced:</strong> Bullet had only 1 seat remaining; booking 2 seats was safely blocked.
                    </div>
                  ) : (
                    <div style={{ marginTop: '8px', fontSize: '12px', color: 'var(--ink-secondary)', display: 'flex', gap: '16px' }}>
                      <span>Passenger: <strong>{item.passenger}</strong></span>
                      <span>Occupied: <strong>{item.occupiedSeats} / 3 Seats</strong></span>
                      <span>Status: <strong>{item.status || item.poolStatus}</strong></span>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        ) : (
          <div style={{ padding: '24px', textAlign: 'center', color: 'var(--ink-secondary)', backgroundColor: 'var(--bg-subtle)', borderRadius: 'var(--radius-sm)' }}>
            <p style={{ fontSize: '13px' }}>
              Click <strong>"Execute Rush-Hour Story"</strong> to simulate the complete Banani narrative from the challenge brief: Nusrat booking, Rafiq pooling, Shirin's capacity rejection, and the final 3rd seat fill.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
