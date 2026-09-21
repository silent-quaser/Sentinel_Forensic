import React, { useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
import { Link } from 'react-router-dom';
import { healthApi } from '../../api/client';
import { SystemHealth } from '../../types';

interface TopHeaderProps {
  title?: string;
  subtitle?: string;
}

export const TopHeader: React.FC<TopHeaderProps> = ({ title, subtitle }) => {
  const [health, setHealth] = useState<SystemHealth | null>(null);

  useEffect(() => {
    healthApi.getHealth().then(setHealth).catch(() => null);
  }, []);

  return (
    <header className="h-16 px-8 border-b border-slate-800 bg-slate-950/60 backdrop-blur-md flex items-center justify-between shrink-0">
      <div>
        {title && <h1 className="text-lg font-bold text-white tracking-tight">{title}</h1>}
        {subtitle && <p className="text-xs text-slate-400">{subtitle}</p>}
      </div>

      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-900 border border-slate-800 text-xs">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
          </span>
          <span className="text-slate-400 font-mono text-[11px]">
            SOC ENGINE: <span className="text-emerald-400 font-semibold">{health?.status || 'ONLINE'}</span>
          </span>
        </div>

        <Link
          to="/investigations/new"
          className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold shadow-[0_0_10px_rgba(6,182,212,0.3)] transition-all"
        >
          <Plus size={16} />
          <span>New Case</span>
        </Link>
      </div>
    </header>
  );
};
