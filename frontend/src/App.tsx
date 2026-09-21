import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Sidebar } from './components/layout/Sidebar';
import { TopHeader } from './components/layout/TopHeader';

import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import InvestigationsList from './pages/InvestigationsList';
import CreateInvestigation from './pages/CreateInvestigation';
import InvestigationDetail from './pages/InvestigationDetail';
import GlobalLogs from './pages/GlobalLogs';
import GlobalThreats from './pages/GlobalThreats';
import ThreatRules from './pages/ThreatRules';
import Reports from './pages/Reports';
import Settings from './pages/Settings';

const AppLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated && location.pathname !== '/login') {
    return <Navigate to="/login" replace />;
  }

  if (location.pathname === '/login') {
    return <>{children}</>;
  }

  return (
    <div className="flex h-screen w-screen bg-[#090d16] text-slate-100 overflow-hidden font-sans">
      <Sidebar />
      <div className="flex-1 flex flex-col h-full overflow-hidden min-w-0">
        <TopHeader />
        <main className="flex-1 overflow-y-auto px-8 py-6">
          {children}
        </main>
      </div>
    </div>
  );
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <AppLayout>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<Dashboard />} />
            <Route path="/investigations" element={<InvestigationsList />} />
            <Route path="/investigations/new" element={<CreateInvestigation />} />
            <Route path="/investigations/:id" element={<InvestigationDetail />} />
            <Route path="/logs" element={<GlobalLogs />} />
            <Route path="/threats" element={<GlobalThreats />} />
            <Route path="/rules" element={<ThreatRules />} />
            <Route path="/reports" element={<Reports />} />
            <Route path="/settings" element={<Settings />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AppLayout>
      </Router>
    </AuthProvider>
  );
}

export default App;
