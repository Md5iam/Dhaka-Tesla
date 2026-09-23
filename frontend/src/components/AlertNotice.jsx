import React from 'react';

export default function AlertNotice({ toasts, removeToast }) {
  if (!toasts || toasts.length === 0) return null;

  return (
    <div className="toast-container">
      {toasts.map((t) => (
        <div
          key={t.id}
          className={`toast ${t.type === 'error' ? 'error' : ''}`}
          onClick={() => removeToast(t.id)}
          style={{ cursor: 'pointer' }}
        >
          <span>{t.message}</span>
        </div>
      ))}
    </div>
  );
}
