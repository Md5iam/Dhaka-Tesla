import React, { useState, useEffect } from 'react';
import { api } from '../api/apiService';
import { Power, Car, Users, CheckCircle, Navigation as NavIcon } from 'lucide-react';

export default function DriverView({ currentUser, onRefreshUserData, addToast }) {
  const [vehicle, setVehicle] = useState(null);
  const [activePool, setActivePool] = useState(null);
  const [passengers, setPassengers] = useState([]);
  const [loadingAction, setLoadingAction] = useState(false);

  useEffect(() => {
    if (currentUser?.id) {
      loadDriverData();
    }
  }, [currentUser?.id]);

  const loadDriverData = async () => {
    try {
      const v = await api.getDriverVehicle(currentUser.id);
      setVehicle(v);
      const pool = await api.getDriverActivePool(currentUser.id);
      setActivePool(pool);
      const pax = await api.getDriverPassengers(currentUser.id);
      setPassengers(pax || []);
    } catch (err) {
    }
  };

  const handleToggleOnline = async () => {
    if (!vehicle) return;
    try {
      const newStatus = vehicle.status === 'ONLINE' ? false : true;
      const updated = await api.toggleDriverOnline(currentUser.id, newStatus);
      setVehicle(updated);
      addToast(`Status updated to ${updated.status}`, 'success');
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  const handleAdvanceStage = async (targetStatus) => {
    if (!activePool) return;
    try {
      setLoadingAction(true);
      await api.advancePoolStage(activePool.id, targetStatus);
      addToast(`Trip advanced to ${targetStatus}`, 'success');
      await onRefreshUserData();
      await loadDriverData();
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setLoadingAction(false);
    }
  };

  const occupiedCount = activePool?.occupiedSeats || 0;
  const totalCapacity = vehicle?.capacity || 3;

  return (
    <div>
      <div className="banner-quote">
        <div>
          <div className="banner-quote-text">
            "Jashim is leaning against Bullet, his three-seat, battery-powered, entirely unaffiliated 'Tesla'."
          </div>
          <div className="banner-quote-sub">
            Driver Cockpit — Managing Bullet's 3-Seat Capacity & Passenger Pooling
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <span className={`badge ${vehicle?.status === 'ONLINE' ? 'badge-green' : 'badge-amber'}`}>
            {vehicle?.status || 'ONLINE'}
          </span>
          <button
            type="button"
            className="btn-outline"
            onClick={handleToggleOnline}
            style={{ fontSize: '12px', padding: '6px 14px' }}
          >
            <Power size={13} />
            <span>{vehicle?.status === 'ONLINE' ? 'Go Offline' : 'Go Online'}</span>
          </button>
        </div>
      </div>

      <div className="grid-two-column">
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <h2 className="editorial-serif" style={{ fontSize: '20px' }}>
              Bullet's Seat Capacity
            </h2>
            <span className={`badge ${occupiedCount >= totalCapacity ? 'badge-amber' : 'badge-green'}`}>
              {occupiedCount} / {totalCapacity} Seats Booked
            </span>
          </div>

          <p style={{ fontSize: '13px', color: 'var(--ink-secondary)', marginBottom: '16px' }}>
            Vehicle: <strong>{vehicle?.modelName || 'Bullet'}</strong> • Plate: {vehicle?.licensePlate || 'DHAKA-METRO-TA-1122'}
          </p>

          <div className="seats-diagram">
            {[0, 1, 2].map((idx) => {
              const paxForSeat = passengers[idx];
              const isOccupied = idx < occupiedCount;
              return (
                <div
                  key={idx}
                  className={`seat-slot ${isOccupied ? 'occupied' : 'available'}`}
                >
                  <Users size={20} color={isOccupied ? '#CC785C' : '#2E7D32'} />
                  <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--ink-primary)' }}>
                    Seat {idx + 1}
                  </span>
                  <span style={{ fontSize: '11px', color: 'var(--ink-secondary)' }}>
                    {paxForSeat ? paxForSeat.passenger.name : isOccupied ? 'Occupied' : 'Vacant'}
                  </span>
                </div>
              );
            })}
          </div>

          {activePool && (
            <div style={{ borderTop: 'var(--hairline)', paddingTop: '18px', marginTop: '16px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', fontSize: '13px' }}>
                <span style={{ color: 'var(--ink-secondary)' }}>Pool Route</span>
                <span style={{ fontWeight: 500 }}>{activePool.startZone} → {activePool.destinationCorridor}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', fontSize: '13px' }}>
                <span style={{ color: 'var(--ink-secondary)' }}>Pool Status</span>
                <span className="badge badge-clay">{activePool.status}</span>
              </div>
            </div>
          )}

          <div style={{ marginTop: '24px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {activePool && (activePool.status === 'OPEN' || activePool.status === 'FULL') && (
              <button
                type="button"
                className="btn-clay"
                style={{ width: '100%', justifyContent: 'center' }}
                onClick={() => handleAdvanceStage('IN_PROGRESS')}
                disabled={loadingAction || passengers.length === 0}
              >
                <NavIcon size={14} />
                <span>Start Trip with {passengers.length} Passenger(s)</span>
              </button>
            )}

            {activePool && activePool.status === 'IN_PROGRESS' && (
              <button
                type="button"
                className="btn-dark"
                style={{ width: '100%' }}
                onClick={() => handleAdvanceStage('COMPLETED')}
                disabled={loadingAction}
              >
                <CheckCircle size={14} />
                <span>Complete Trip & Collect Fares</span>
              </button>
            )}

            {!activePool && (
              <p style={{ textAlign: 'center', fontSize: '13px', color: 'var(--ink-secondary)', padding: '12px' }}>
                No active pool trip right now. Bullet is waiting for rush-hour passenger requests.
              </p>
            )}
          </div>
        </div>

        <div className="card">
          <h2 className="editorial-serif" style={{ fontSize: '20px', marginBottom: '16px' }}>
            Current Passenger Manifest
          </h2>

          {passengers.length > 0 ? (
            <table className="table-minimal">
              <thead>
                <tr>
                  <th>Passenger</th>
                  <th>Pickup</th>
                  <th>Destination</th>
                  <th>Seats</th>
                  <th>Fare</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {passengers.map((p) => (
                  <tr key={p.id}>
                    <td style={{ fontWeight: 600 }}>{p.passenger.name}</td>
                    <td>{p.pickupZone}</td>
                    <td>{p.destinationZone}</td>
                    <td>{p.requestedSeats}</td>
                    <td style={{ color: 'var(--accent-clay)', fontWeight: 600 }}>
                      {Number(p.totalFare).toFixed(2)} BDT
                    </td>
                    <td>
                      <span className="badge badge-green">{p.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div style={{ textAlign: 'center', padding: '40px 16px', color: 'var(--ink-secondary)' }}>
              <Users size={32} style={{ marginBottom: '12px', opacity: 0.4 }} />
              <p className="editorial-serif" style={{ fontSize: '16px', color: 'var(--ink-primary)' }}>
                No Passengers Assigned
              </p>
              <p style={{ fontSize: '13px' }}>
                When Nusrat, Rafiq, or Shirin book a seat, they will appear here.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
