import React, { useState } from 'react';
import { X } from 'lucide-react';

export default function AuthModal({ isOpen, onClose, onLogin, onRegister, addToast }) {
  if (!isOpen) return null;

  const [activeTab, setActiveTab] = useState('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [role, setRole] = useState('PASSENGER');
  const [vehicleModel, setVehicleModel] = useState('Bullet');
  const [licensePlate, setLicensePlate] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      addToast('Please provide both email and password', 'error');
      return;
    }
    setLoading(true);
    try {
      await onLogin({ email, password });
      onClose();
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) {
      addToast('Name is required', 'error');
      return;
    }
    if (!email.includes('@')) {
      addToast('Valid email is required', 'error');
      return;
    }
    if (password.length < 4) {
      addToast('Password must be at least 4 characters', 'error');
      return;
    }

    setLoading(true);
    try {
      await onRegister({
        name,
        email,
        password,
        phone,
        role,
        vehicleModelName: role === 'DRIVER' ? vehicleModel : null,
        vehicleLicensePlate: role === 'DRIVER' ? licensePlate : null,
      });
      onClose();
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const quickFill = (demoEmail, demoPassword) => {
    setEmail(demoEmail);
    setPassword(demoPassword);
    setActiveTab('login');
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="editorial-serif" style={{ fontSize: '24px' }}>
            {activeTab === 'login' ? 'Sign In' : 'Create Account'}
          </h2>
          <button className="modal-close-btn" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <div className="modal-tabs">
          <button
            className={`modal-tab-btn ${activeTab === 'login' ? 'active' : ''}`}
            onClick={() => setActiveTab('login')}
          >
            Sign In
          </button>
          <button
            className={`modal-tab-btn ${activeTab === 'register' ? 'active' : ''}`}
            onClick={() => setActiveTab('register')}
          >
            New Account
          </button>
        </div>

        {activeTab === 'login' ? (
          <form onSubmit={handleLoginSubmit}>
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input
                type="email"
                className="form-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="e.g. nusrat@dhakatesla.com"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <input
                type="password"
                className="form-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
              />
            </div>

            <button
              type="submit"
              className="btn-dark"
              style={{ width: '100%', marginTop: '8px' }}
              disabled={loading}
            >
              {loading ? 'Authenticating...' : 'Sign In'}
            </button>

            <div style={{ marginTop: '24px', paddingTop: '16px', borderTop: 'var(--hairline)' }}>
              <span className="form-label" style={{ marginBottom: '8px' }}>
                Or Demo Quick-Fill
              </span>
              <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                <button
                  type="button"
                  className="btn-outline"
                  style={{ fontSize: '11px', padding: '4px 10px' }}
                  onClick={() => quickFill('nusrat@dhakatesla.com', 'password123')}
                >
                  Nusrat
                </button>
                <button
                  type="button"
                  className="btn-outline"
                  style={{ fontSize: '11px', padding: '4px 10px' }}
                  onClick={() => quickFill('rafiq@dhakatesla.com', 'password123')}
                >
                  Rafiq
                </button>
                <button
                  type="button"
                  className="btn-outline"
                  style={{ fontSize: '11px', padding: '4px 10px' }}
                  onClick={() => quickFill('shirin@dhakatesla.com', 'password123')}
                >
                  Shirin
                </button>
                <button
                  type="button"
                  className="btn-outline"
                  style={{ fontSize: '11px', padding: '4px 10px' }}
                  onClick={() => quickFill('jashim@dhakatesla.com', 'password123')}
                >
                  Jashim (Driver)
                </button>
              </div>
            </div>
          </form>
        ) : (
          <form onSubmit={handleRegisterSubmit}>
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input
                type="text"
                className="form-input"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Your name"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input
                type="email"
                className="form-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@example.com"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <input
                type="password"
                className="form-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="At least 4 characters"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Phone Number</label>
              <input
                type="text"
                className="form-input"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="+8801700000000"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Role</label>
              <div className="seat-selector-row">
                <button
                  type="button"
                  className={`seat-btn ${role === 'PASSENGER' ? 'active' : ''}`}
                  onClick={() => setRole('PASSENGER')}
                >
                  Passenger
                </button>
                <button
                  type="button"
                  className={`seat-btn ${role === 'DRIVER' ? 'active' : ''}`}
                  onClick={() => setRole('DRIVER')}
                >
                  Driver
                </button>
              </div>
            </div>

            {role === 'DRIVER' && (
              <>
                <div className="form-group">
                  <label className="form-label">Vehicle Name</label>
                  <input
                    type="text"
                    className="form-input"
                    value={vehicleModel}
                    onChange={(e) => setVehicleModel(e.target.value)}
                    placeholder="e.g. Bullet"
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">License Plate</label>
                  <input
                    type="text"
                    className="form-input"
                    value={licensePlate}
                    onChange={(e) => setLicensePlate(e.target.value)}
                    placeholder="e.g. DHAKA-METRO-1234"
                  />
                </div>
              </>
            )}

            <button
              type="submit"
              className="btn-dark"
              style={{ width: '100%', marginTop: '8px' }}
              disabled={loading}
            >
              {loading ? 'Creating...' : 'Register'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
