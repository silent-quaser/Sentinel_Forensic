import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  Shield,
  LayoutDashboard,
  FolderLock,
  PlusCircle,
  FileSearch,
  AlertOctagon,
  Sliders,
  FileBarChart2,
  Activity,
  LogOut
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const Sidebar: React.FC = () => {
  const { user, logout } = useAuth();

  const navItems = [
    { to: '/', label: 'SOC Dashboard', icon: LayoutDashboard },
    { to: '/investigations', label: 'Investigations', icon: FolderLock },
    { to: '/investigations/new', label: 'New Investigation', icon: PlusCircle },
    { to: '/logs', label: 'Event Explorer', icon: FileSearch },
    { to: '/threats', label: 'Threat Center', icon: AlertOctagon },
    { to: '/rules', label: 'Detection Rules', icon: Sliders },
    { to: '/reports', label: 'Forensic Reports', icon: FileBarChart2 },
    { to: '/settings', label: 'System Health', icon: Activity },
  ];

  return (
    <aside className="w-64 bg-slate-950 border-r border-slate-800 flex flex-col h-screen shrink-0 select-none">
      <div className="h-16 px-6 border-b border-slate-800 flex items-center gap-3 bg-slate-950/80">
        <div className="w-9 h-9 rounded-lg bg-gradient-to-tr from-cyan-600 to-blue-600 flex items-center justify-center shadow-[0_0_12px_rgba(6,182,212,0.4)]">
          <Shield size={20} className="text-white" />
        </div>
        <div>
          <div className="font-bold font-mono tracking-wider text-sm text-white flex items-center gap-1.5">
            <span>SENTINEL</span>
            <span className="text-cyan-400">FORENSIC</span>
          </div>
          <span className="text-[10px] text-slate-400 font-mono block tracking-tight">
            INCIDENT TRIAGE PLATFORM
          </span>
        </div>
      </div>

      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        <div className="px-3 pb-2 text-[10px] font-semibold uppercase tracking-wider text-slate-400">
          Core Operations
        </div>
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-xs font-medium transition-all ${
                  isActive
                    ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/30 shadow-[0_0_10px_rgba(6,182,212,0.15)]'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900 border border-transparent'
                }`
              }
            >
              <Icon size={18} />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div className="p-3 border-t border-slate-800 bg-slate-950/90">
        <div className="p-2.5 rounded-lg bg-slate-900/80 border border-slate-800/80 flex items-center justify-between">
          <div className="flex items-center gap-2.5 min-w-0">
            <div className="w-8 h-8 rounded-full bg-cyan-950 border border-cyan-500/40 flex items-center justify-center text-cyan-400 shrink-0 font-bold text-xs font-mono">
              N
            </div>
            <div className="min-w-0">
              <p className="text-xs font-semibold text-slate-200 truncate">
                {user?.fullName || 'Naveen'}
              </p>
              <p className="text-[10px] text-cyan-400 font-mono truncate">
                {user?.badgeId || 'SF-INV-019'}
              </p>
            </div>
          </div>
          <button
            onClick={logout}
            title="Logout / Switch Investigator"
            className="p-1.5 text-slate-400 hover:text-red-400 hover:bg-slate-800 rounded transition-colors"
          >
            <LogOut size={16} />
          </button>
        </div>
      </div>
    </aside>
  );
};
