const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  };

  try {
    const res = await fetch(url, config);
    const data = await res.json();
    if (!res.ok || data.success === false) {
      let errorMessage = data.message || 'Request failed';
      if (data.errors && typeof data.errors === 'object') {
        const fieldErrors = Object.entries(data.errors)
          .map(([field, msg]) => `${field}: ${msg}`)
          .join(', ');
        if (fieldErrors) {
          errorMessage = `${errorMessage} (${fieldErrors})`;
        }
      }
      throw new Error(errorMessage);
    }
    return data.data;
  } catch (err) {
    throw err;
  }
}

export const api = {
  getZones: () => request('/zones'),
  getUsers: () => request('/auth/users'),
  getUserById: (id) => request(`/auth/users/${id}`),
  login: (credentials) => request('/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  }),
  register: (payload) => request('/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  topUpWallet: (id, amount) => request(`/auth/users/${id}/topup`, {
    method: 'POST',
    body: JSON.stringify({ amount }),
  }),
  estimateFare: (pickupZone, destinationZone, requestedSeats = 1) =>
    request(`/rides/estimate?pickupZone=${encodeURIComponent(pickupZone)}&destinationZone=${encodeURIComponent(destinationZone)}&requestedSeats=${requestedSeats}`),
  bookRide: (payload) => request('/rides/book', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  getRideById: (id) => request(`/rides/${id}`),
  getPassengerRides: (passengerId) => request(`/rides/passenger/${passengerId}`),
  getActivePassengerRide: (passengerId) => request(`/rides/passenger/${passengerId}/active`),
  cancelRide: (id) => request(`/rides/${id}/cancel`, {
    method: 'POST',
  }),
  toggleDriverOnline: (driverId, online) => request(`/drivers/${driverId}/toggle-online`, {
    method: 'POST',
    body: JSON.stringify({ online }),
  }),
  getDriverVehicle: (driverId) => request(`/drivers/${driverId}/vehicle`),
  getDriverActivePool: (driverId) => request(`/drivers/${driverId}/active-pool`),
  getDriverPassengers: (driverId) => request(`/drivers/${driverId}/passengers`),
  advancePoolStage: (poolId, targetStatus) => request(`/drivers/pools/${poolId}/advance`, {
    method: 'POST',
    body: JSON.stringify({ targetStatus }),
  }),
  runRushHourSimulation: () => request('/simulation/rush-hour', {
    method: 'POST',
  }),
  resetSimulation: () => request('/simulation/reset', {
    method: 'POST',
  }),
};
