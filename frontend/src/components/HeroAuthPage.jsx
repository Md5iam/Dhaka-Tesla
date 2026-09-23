import React, { useState } from 'react';
import { Play, ArrowRight, ShieldCheck, Zap } from 'lucide-react';

export default function HeroAuthPage({ onLogin, onRegister, onOpenSimulation, addToast }) {
  const [activeTab, setActiveTab] = useState('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [role, setRole] = useState('PASSENGER');
  const [vehicleModel, setVehicleModel] = useState('');
  const [licensePlate, setLicensePlate] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      addToast('Please enter both email and password', 'error');
      return;
    }
    setLoading(true);
    try {
      await onLogin({ email, password });
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
        vehicleModelName: role === 'DRIVER' ? (vehicleModel || 'Bullet') : null,
        vehicleLicensePlate: role === 'DRIVER' ? (licensePlate || 'DHAKA-METRO-TA-1122') : null,
      });
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickDemoLogin = async (demoEmail, demoPassword) => {
    setLoading(true);
    try {
      await onLogin({ email: demoEmail, password: demoPassword });
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="hero-wrapper">
      <div className="hero-grid">
        <div>
          <div className="hero-pill-badge">
            <Zap size={14} color="#CC785C" />
            <span>Banani Rush-Hour Ride Pooling</span>
          </div>

          <h1 className="hero-title">
            Share a seat. Split the fare. Survive Dhaka traffic.
          </h1>

          <p className="hero-description">
            8:41 AM, Banani Road 11. Three passengers need to reach Mohakhali and Gulshan corridors. Jashim is waiting with Bullet, his 3-seat electric Tesla. Our system calculates transparent fares, pools compatible routes, and strictly guarantees that seats never exceed capacity.
          </p>

          <div className="hero-story-excerpt">
            <div className="hero-story-excerpt-text">
              "Everyone just wants to get where they're going, pay a fair price, and not accidentally make a new friend."
            </div>
            <div style={{ fontSize: '13px', color: 'var(--ink-secondary)' }}>
              — The Banani Rush-Hour Brief
            </div>
          </div>

          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            <button
              type="button"
              className="btn-clay"
              onClick={onOpenSimulation}
              style={{ padding: '10px 20px', fontSize: '14px' }}
            >
              <Play size={14} fill="#FFFFFF" />
              <span>Simulate Rush-Hour Story</span>
            </button>
          </div>
        </div>

        <div className="hero-auth-card">
          <div className="modal-tabs" style={{ marginBottom: '20px' }}>
            <button
              type="button"
              className={`modal-tab-btn ${activeTab === 'login' ? 'active' : ''}`}
              onClick={() => setActiveTab('login')}
            >
              Sign In
            </button>
            <button
              type="button"
              className={`modal-tab-btn ${activeTab === 'register' ? 'active' : ''}`}
              onClick={() => setActiveTab('register')}
            >
              Create Account
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
                  placeholder="Enter your email"
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
                  placeholder="Enter your password"
                  required
                />
              </div>

              <button
                type="submit"
                className="btn-dark"
                style={{ width: '100%', marginTop: '12px' }}
                disabled={loading}
              >
                {loading ? 'Authenticating...' : 'Sign In to Dashboard'}
              </button>

              <div className="hero-quick-demo">
                <span className="form-label" style={{ marginBottom: '6px' }}>
                  Demo Accounts (1-Click Login)
                </span>
                <div className="demo-actor-grid">
                  <button
                    type="button"
                    className="demo-actor-btn"
                    onClick={() => handleQuickDemoLogin('nusrat@dhakatesla.com', 'password123')}
                    disabled={loading}
                  >
                    <strong>Nusrat</strong>
                    <span className="demo-actor-role">Passenger (Mohakhali)</span>
                  </button>

                  <button
                    type="button"
                    className="demo-actor-btn"
                    onClick={() => handleQuickDemoLogin('rafiq@dhakatesla.com', 'password123')}
                    disabled={loading}
                  >
                    <strong>Rafiq</strong>
                    <span className="demo-actor-role">Passenger (Gulshan 1)</span>
                  </button>

                  <button
                    type="button"
                    className="demo-actor-btn"
                    onClick={() => handleQuickDemoLogin('shirin@dhakatesla.com', 'password123')}
                    disabled={loading}
                  >
                    <strong>Shirin</strong>
                    <span className="demo-actor-role">Passenger (3rd Seat)</span>
                  </button>

                  <button
                    type="button"
                    className="demo-actor-btn"
                    onClick={() => handleQuickDemoLogin('jashim@dhakatesla.com', 'password123')}
                    disabled={loading}
                  >
                    <strong>Jashim</strong>
                    <span className="demo-actor-role">Driver of Bullet (3 Seats)</span>
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
                  placeholder="e.g. Nusrat Jahan"
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
                  placeholder="e.g. nusrat@example.com"
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
                  placeholder="+880 1700 000000"
                />
              </div>

              <div className="form-group">
                <label className="form-label">Account Role</label>
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
                      placeholder="e.g. DHAKA-METRO-TA-1122"
                    />
                  </div>
                </>
              )}

              <button
                type="submit"
                className="btn-dark"
                style={{ width: '100%', marginTop: '12px' }}
                disabled={loading}
              >
                {loading ? 'Creating...' : 'Register & Enter Dashboard'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
