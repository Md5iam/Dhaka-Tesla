import React, { useState, useEffect } from 'react';
import { api } from '../api/apiService';
import { Clock, CheckCircle2, AlertCircle, XCircle } from 'lucide-react';

export default function PassengerView({ currentUser, zones, onRefreshUserData, addToast }) {
  const [pickupZone, setPickupZone] = useState('');
  const [destinationZone, setDestinationZone] = useState('');
  const [requestedSeats, setRequestedSeats] = useState(1);
  const [fareEstimate, setFareEstimate] = useState(null);
  const [activeRide, setActiveRide] = useState(null);
  const [rideHistory, setRideHistory] = useState([]);
  const [loadingEstimate, setLoadingEstimate] = useState(false);
  const [bookingLoading, setBookingLoading] = useState(false);

  useEffect(() => {
    if (pickupZone && destinationZone && pickupZone !== destinationZone) {
      fetchEstimate();
    } else {
      setFareEstimate(null);
    }
  }, [pickupZone, destinationZone, requestedSeats]);

  useEffect(() => {
    if (currentUser?.id) {
      loadActiveRideAndHistory();
    }
  }, [currentUser?.id]);

  const fetchEstimate = async () => {
    try {
      setLoadingEstimate(true);
      const est = await api.estimateFare(pickupZone, destinationZone, requestedSeats);
      setFareEstimate(est);
    } catch (err) {
      setFareEstimate(null);
    } finally {
      setLoadingEstimate(false);
    }
  };

  const loadActiveRideAndHistory = async () => {
    try {
      const active = await api.getActivePassengerRide(currentUser.id);
      setActiveRide(active);
      const history = await api.getPassengerRides(currentUser.id);
      setRideHistory(history || []);
    } catch (err) {
    }
  };

  const handleBookRide = async () => {
    if (!pickupZone || !destinationZone) {
      addToast('Please select both pickup and destination zones', 'error');
      return;
    }
    if (pickupZone === destinationZone) {
      addToast('Pickup and destination cannot be identical', 'error');
      return;
    }
    if (currentUser.walletBalance < fareEstimate?.pooledFare) {
      addToast('Insufficient TeslaPay balance. Please top up.', 'error');
      return;
    }

    try {
      setBookingLoading(true);
      const ride = await api.bookRide({
        passengerId: currentUser.id,
        pickupZone,
        destinationZone,
        requestedSeats,
      });
      setActiveRide(ride);
      addToast('Tesla Pool ride matched successfully!', 'success');
      await onRefreshUserData();
      await loadActiveRideAndHistory();
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setBookingLoading(false);
    }
  };

  const handleCancelRide = async (rideId) => {
    try {
      await api.cancelRide(rideId);
      addToast('Ride cancelled and fare refunded to wallet', 'success');
      await onRefreshUserData();
      await loadActiveRideAndHistory();
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  const getStepStatus = (stepName) => {
    if (!activeRide) return '';
    const status = activeRide.status;

    const order = ['REQUESTED', 'MATCHED', 'DRIVER_ARRIVED', 'STARTED', 'COMPLETED'];
    const currentIndex = order.indexOf(status);
    const stepIndex = order.indexOf(stepName);

    if (currentIndex > stepIndex) return 'completed';
    if (currentIndex === stepIndex) return 'active';
    return '';
  };

  return (
    <div>
      <div className="banner-quote">
        <div>
          <div className="banner-quote-text">
            Welcome, {currentUser.name}
          </div>
          <div className="banner-quote-sub">
            Passenger Account: {currentUser.email} • TeslaPay: {Number(currentUser.walletBalance).toFixed(2)} BDT
          </div>
        </div>
        <div>
          <span className="badge badge-clay">Role: Passenger</span>
        </div>
      </div>

      <div className="grid-two-column">
        <div className="card">
          <h2 className="editorial-serif" style={{ fontSize: '20px', marginBottom: '20px' }}>
            Book a Seat in Bullet
          </h2>

          <div className="form-group">
            <label className="form-label">Pickup Point</label>
            <select
              className="form-select"
              value={pickupZone}
              onChange={(e) => setPickupZone(e.target.value)}
            >
              <option value="">Select pickup zone...</option>
              {zones.map((z) => (
                <option key={z.id} value={z.name}>
                  {z.name} ({z.corridorGroup})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Destination</label>
            <select
              className="form-select"
              value={destinationZone}
              onChange={(e) => setDestinationZone(e.target.value)}
            >
              <option value="">Select destination...</option>
              {zones.map((z) => (
                <option key={z.id} value={z.name}>
                  {z.name} ({z.corridorGroup})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Seats Required</label>
            <div className="seat-selector-row">
              {[1, 2, 3].map((s) => (
                <button
                  key={s}
                  type="button"
                  className={`seat-btn ${requestedSeats === s ? 'active' : ''}`}
                  onClick={() => setRequestedSeats(s)}
                >
                  {s} {s === 1 ? 'Seat' : 'Seats'}
                </button>
              ))}
            </div>
          </div>

          {fareEstimate ? (
            <div className="fare-breakdown-box">
              <div className="fare-row">
                <span>Estimated Distance</span>
                <span>{fareEstimate.distanceKm} km</span>
              </div>
              <div className="fare-row">
                <span>Base Fare</span>
                <span>{Number(fareEstimate.baseFare).toFixed(2)} BDT</span>
              </div>
              <div className="fare-row">
                <span>Distance Charge ({requestedSeats}x)</span>
                <span>{Number(fareEstimate.distanceCharge).toFixed(2)} BDT</span>
              </div>
              <div className="fare-row">
                <span>Solo Unpooled Fare</span>
                <span>{Number(fareEstimate.soloFare).toFixed(2)} BDT</span>
              </div>
              <div className="fare-row highlight">
                <span>Tesla Pool Discount (20% off)</span>
                <span>- {Number(fareEstimate.poolDiscount).toFixed(2)} BDT</span>
              </div>
              <div className="fare-divider" />
              <div className="fare-total-row">
                <span className="fare-total-label">Your Pooled Fare</span>
                <span className="fare-total-amount">
                  {Number(fareEstimate.pooledFare).toFixed(2)} BDT
                </span>
              </div>
            </div>
          ) : (
            <div style={{ padding: '16px', background: 'var(--bg-subtle)', borderRadius: 'var(--radius-sm)', margin: '20px 0', fontSize: '13px', color: 'var(--ink-secondary)', textAlign: 'center' }}>
              Select pickup and destination above to see transparent fare calculation.
            </div>
          )}

          <button
            type="button"
            className="btn-dark"
            style={{ width: '100%' }}
            onClick={handleBookRide}
            disabled={bookingLoading || activeRide !== null || !fareEstimate}
          >
            {activeRide ? 'Active Ride In Progress' : bookingLoading ? 'Reserving Seat...' : 'Request Tesla Pool'}
          </button>
        </div>

        <div className="card">
          <h2 className="editorial-serif" style={{ fontSize: '20px', marginBottom: '20px' }}>
            Live Trip Status
          </h2>

          {activeRide ? (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <span className="badge badge-green">
                  Ride #{activeRide.id} • {activeRide.status}
                </span>
                <span style={{ fontSize: '13px', color: 'var(--ink-secondary)' }}>
                  Vehicle: {activeRide.pool?.vehicle?.modelName || 'Bullet'}
                </span>
              </div>

              <div className="stepper">
                <div className={`step-item ${getStepStatus('REQUESTED')}`}>
                  <div className="step-bubble">1</div>
                  <span className="step-label">Requested</span>
                </div>
                <div className={`step-item ${getStepStatus('MATCHED')}`}>
                  <div className="step-bubble">2</div>
                  <span className="step-label">Matched</span>
                </div>
                <div className={`step-item ${getStepStatus('DRIVER_ARRIVED')}`}>
                  <div className="step-bubble">3</div>
                  <span className="step-label">Arrived</span>
                </div>
                <div className={`step-item ${getStepStatus('STARTED')}`}>
                  <div className="step-bubble">4</div>
                  <span className="step-label">On Trip</span>
                </div>
                <div className={`step-item ${getStepStatus('COMPLETED')}`}>
                  <div className="step-bubble">5</div>
                  <span className="step-label">Finished</span>
                </div>
              </div>

              <div style={{ border: 'var(--hairline)', borderRadius: 'var(--radius-sm)', padding: '16px', marginBottom: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                  <span style={{ color: 'var(--ink-secondary)', fontSize: '13px' }}>Route</span>
                  <span style={{ fontWeight: 500 }}>{activeRide.pickupZone} → {activeRide.destinationZone}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                  <span style={{ color: 'var(--ink-secondary)', fontSize: '13px' }}>Tesla Assigned</span>
                  <span style={{ fontWeight: 500 }}>
                    {activeRide.pool?.vehicle?.modelName || 'Bullet'} ({activeRide.pool?.vehicle?.licensePlate || 'DHAKA-METRO-TA-1122'})
                  </span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                  <span style={{ color: 'var(--ink-secondary)', fontSize: '13px' }}>Driver</span>
                  <span style={{ fontWeight: 500 }}>Jashim</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                  <span style={{ color: 'var(--ink-secondary)', fontSize: '13px' }}>Seats Booked</span>
                  <span style={{ fontWeight: 500 }}>{activeRide.requestedSeats} Seat(s)</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--ink-secondary)', fontSize: '13px' }}>Deducted Fare</span>
                  <span style={{ fontWeight: 600, color: 'var(--accent-clay)' }}>
                    {Number(activeRide.totalFare).toFixed(2)} BDT
                  </span>
                </div>
              </div>

              {(activeRide.status === 'MATCHED' || activeRide.status === 'REQUESTED') && (
                <button
                  type="button"
                  className="btn-outline"
                  style={{ width: '100%', color: 'var(--status-red)', borderColor: 'rgba(185, 28, 28, 0.3)' }}
                  onClick={() => handleCancelRide(activeRide.id)}
                >
                  <XCircle size={14} />
                  <span>Cancel Ride & Refund Fare</span>
                </button>
              )}
            </div>
          ) : (
            <div style={{ textAlign: 'center', padding: '48px 24px', color: 'var(--ink-secondary)' }}>
              <Clock size={32} style={{ marginBottom: '12px', opacity: 0.4 }} />
              <p className="editorial-serif" style={{ fontSize: '18px', color: 'var(--ink-primary)', marginBottom: '6px' }}>
                No Active Pool Trip
              </p>
              <p style={{ fontSize: '13px' }}>
                Select your pickup and drop-off zones on the left to join Jashim's Bullet.
              </p>
            </div>
          )}
        </div>
      </div>

      <div className="card" style={{ marginTop: '28px' }}>
        <h3 className="editorial-serif" style={{ fontSize: '18px', marginBottom: '16px' }}>
          Ride History for {currentUser.name}
        </h3>
        {rideHistory.length > 0 ? (
          <table className="table-minimal">
            <thead>
              <tr>
                <th>ID</th>
                <th>Route</th>
                <th>Seats</th>
                <th>Fare</th>
                <th>Discount</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {rideHistory.map((r) => (
                <tr key={r.id}>
                  <td>#{r.id}</td>
                  <td>{r.pickupZone} → {r.destinationZone}</td>
                  <td>{r.requestedSeats}</td>
                  <td style={{ fontWeight: 600 }}>{Number(r.totalFare).toFixed(2)} BDT</td>
                  <td style={{ color: 'var(--accent-clay)' }}>-{Number(r.poolDiscount).toFixed(2)} BDT</td>
                  <td>
                    <span className={`badge ${
                      r.status === 'COMPLETED' ? 'badge-green' : r.status === 'CANCELLED' ? 'badge-amber' : 'badge-clay'
                    }`}>
                      {r.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p style={{ fontSize: '13px', color: 'var(--ink-secondary)' }}>No previous rides recorded for this account.</p>
        )}
      </div>
    </div>
  );
}
