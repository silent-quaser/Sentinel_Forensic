import React, { createContext, useContext, useState, useEffect } from 'react';

export interface User {
  username: string;
  fullName: string;
  role: string;
  badgeId: string;
}

interface AuthContextType {
  user: User | null;
  login: (username: string) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const DEFAULT_USER: User = {
  username: 'naveen',
  fullName: 'Naveen (Investigator)',
  role: 'Lead Digital Forensic Analyst',
  badgeId: 'SF-INV-019',
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('sentinel_auth_user');
    return saved ? JSON.parse(saved) : DEFAULT_USER;
  });

  useEffect(() => {
    if (user) {
      localStorage.setItem('sentinel_auth_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('sentinel_auth_user');
    }
  }, [user]);

  const login = (username: string) => {
    const newUser: User = {
      username: username.toLowerCase().trim(),
      fullName: username.toLowerCase().includes('naveen') ? 'Naveen (Investigator)' : username,
      role: 'Forensic Investigator',
      badgeId: `SF-INV-${Math.floor(100 + Math.random() * 900)}`,
    };
    setUser(newUser);
  };

  const logout = () => {
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
