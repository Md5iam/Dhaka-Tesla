import React from 'react';
import { Wallet, Play, RotateCcw, LogOut, User } from 'lucide-react';

export default function Navigation({
  currentUser,
  allUsers,
  onSelectUser,
  onLogout,
  onOpenSimulation,
  onResetSimulation,
  onTopUpWallet,
}) {
  return (
    <header className="editorial-header">
      <div className="header-inner">
        <div className="logo-group">
          <span className="brand-title">Dhaka Tesla Pool</span>
          <span className="brand-tagline">Share a seat. Split the fare.</span>
        </div>

        {currentUser ? (
          <div className="cast-segmented">
            {allUsers.map((u) => {
              const isActive = currentUser.id === u.id;
              return (
                <button
                  key={u.id}
                  className={`cast-pill ${isActive ? 'active' : ''}`}
                  onClick={() => onSelectUser(u)}
                >
                  {u.name} {u.role === 'DRIVER' ? '(Driver)' : ''}
                </button>
              );
            })}
          </div>
        ) : null}

        <div className="header-actions">
          {currentUser && (
            <div className="wallet-badge">
              <Wallet size={14} color="#63615D" />
              <span>{Number(currentUser.walletBalance).toFixed(2)} BDT</span>
              <button
                className="wallet-topup-btn"
                onClick={() => onTopUpWallet(currentUser.id, 200)}
                title="Top up 200 BDT"
              >
                +200
              </button>
            </div>
          )}

          <button
            className="btn-clay"
            onClick={onOpenSimulation}
            title="Interactive Rush-Hour Story"
          >
            <Play size={13} fill="#FFFFFF" />
            <span>Rush Hour</span>
          </button>

          <button
            className="btn-outline"
            onClick={onResetSimulation}
            title="Reset system to initial state"
          >
            <RotateCcw size={13} />
            <span>Reset</span>
          </button>

          {currentUser ? (
            <button
              className="btn-outline"
              onClick={onLogout}
              title="Sign out to Hero page"
            >
              <LogOut size={13} />
              <span>Sign Out</span>
            </button>
          ) : null}
        </div>
      </div>
    </header>
  );
}
