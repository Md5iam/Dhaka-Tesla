import React, { useState, useEffect } from 'react';
import { api } from './api/apiService';
import Navigation from './components/Navigation';
import HeroAuthPage from './components/HeroAuthPage';
import PassengerView from './components/PassengerView';
import DriverView from './components/DriverView';
import AuthModal from './components/AuthModal';
import RushHourStoryModal from './components/RushHourStoryModal';
import AlertNotice from './components/AlertNotice';

export default function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [allUsers, setAllUsers] = useState([]);
  const [zones, setZones] = useState([]);
  const [authModalOpen, setAuthModalOpen] = useState(false);
  const [simulationModalOpen, setSimulationModalOpen] = useState(false);
  const [toasts, setToasts] = useState([]);

  const addToast = (message, type = 'info') => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      removeToast(id);
    }, 4000);
  };

  const removeToast = (id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  useEffect(() => {
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    try {
      const userList = await api.getUsers();
      setAllUsers(userList || []);
      const zoneList = await api.getZones();
      setZones(zoneList || []);
    } catch (err) {
      addToast('Backend connecting... Please ensure Spring Boot is running on port 8080.', 'error');
    }
  };

  const handleSelectUser = (user) => {
    setCurrentUser(user);
    addToast(`Switched active view to ${user.name} (${user.role})`);
  };

  const handleLogout = () => {
    setCurrentUser(null);
    addToast('Signed out successfully. Returned to Home.');
  };

  const handleRefreshUserData = async () => {
    if (!currentUser?.id) return;
    try {
      const refreshed = await api.getUserById(currentUser.id);
      setCurrentUser(refreshed);
      const userList = await api.getUsers();
      setAllUsers(userList || []);
    } catch (err) {
    }
  };

  const handleTopUpWallet = async (userId, amount) => {
    try {
      const newBal = await api.topUpWallet(userId, amount);
      setCurrentUser((prev) => ({ ...prev, walletBalance: newBal }));
      addToast(`Recharged ${amount} BDT to TeslaPay wallet`, 'success');
      const userList = await api.getUsers();
      setAllUsers(userList || []);
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  const handleLogin = async (credentials) => {
    const user = await api.login(credentials);
    setCurrentUser(user);
    const userList = await api.getUsers();
    setAllUsers(userList || []);
    addToast(`Welcome, ${user.name}! (${user.role})`, 'success');
  };

  const handleRegister = async (payload) => {
    const user = await api.register(payload);
    setCurrentUser(user);
    const userList = await api.getUsers();
    setAllUsers(userList || []);
    addToast(`Account created for ${user.name}! (${user.role})`, 'success');
  };

  const handleResetSimulation = async () => {
    try {
      await api.resetSimulation();
      addToast('System reset completed successfully', 'success');
      await loadInitialData();
      if (currentUser?.id) {
        await handleRefreshUserData();
      }
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  return (
    <div className="app-container">
      <Navigation
        currentUser={currentUser}
        allUsers={allUsers}
        onSelectUser={handleSelectUser}
        onLogout={handleLogout}
        onOpenSimulation={() => setSimulationModalOpen(true)}
        onResetSimulation={handleResetSimulation}
        onTopUpWallet={handleTopUpWallet}
      />

      <main className="main-content">
        {!currentUser ? (
          <HeroAuthPage
            onLogin={handleLogin}
            onRegister={handleRegister}
            onOpenSimulation={() => setSimulationModalOpen(true)}
            addToast={addToast}
          />
        ) : currentUser.role === 'DRIVER' ? (
          <DriverView
            currentUser={currentUser}
            onRefreshUserData={handleRefreshUserData}
            addToast={addToast}
          />
        ) : (
          <PassengerView
            currentUser={currentUser}
            zones={zones}
            onRefreshUserData={handleRefreshUserData}
            addToast={addToast}
          />
        )}
      </main>

      <AuthModal
        isOpen={authModalOpen}
        onClose={() => setAuthModalOpen(false)}
        onLogin={handleLogin}
        onRegister={handleRegister}
        addToast={addToast}
      />

      <RushHourStoryModal
        isOpen={simulationModalOpen}
        onClose={() => setSimulationModalOpen(false)}
        onRefreshAll={handleRefreshUserData}
        addToast={addToast}
      />

      <AlertNotice toasts={toasts} removeToast={removeToast} />
    </div>
  );
}
