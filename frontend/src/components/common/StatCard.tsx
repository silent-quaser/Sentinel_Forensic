import React from 'react';
import { LucideIcon } from 'lucide-react';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: LucideIcon;
  color?: 'blue' | 'red' | 'cyan' | 'amber' | 'emerald' | 'purple';
  trend?: string;
}

export const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  subtitle,
  icon: Icon,
  color = 'blue',
  trend,
}) => {
  const colorMap = {
    blue: { icon: 'text-blue-400 bg-blue-500/10 border-blue-500/20', border: 'border-blue-500/20' },
    red: { icon: 'text-red-400 bg-red-500/10 border-red-500/20', border: 'border-red-500/20' },
    cyan: { icon: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/20', border: 'border-cyan-500/20' },
    amber: { icon: 'text-amber-400 bg-amber-500/10 border-amber-500/20', border: 'border-amber-500/20' },
    emerald: { icon: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20', border: 'border-emerald-500/20' },
    purple: { icon: 'text-purple-400 bg-purple-500/10 border-purple-500/20', border: 'border-purple-500/20' },
  };

  const scheme = colorMap[color];

  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-5 shadow-lg relative overflow-hidden backdrop-blur-sm hover:border-slate-700 transition-all">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs uppercase tracking-wider font-semibold text-slate-400">{title}</p>
          <p className="text-2xl lg:text-3xl font-bold font-mono text-white mt-1.5">{value}</p>
          {subtitle && <p className="text-xs text-slate-500 mt-1">{subtitle}</p>}
          {trend && <p className="text-xs text-cyan-400 font-mono mt-1">{trend}</p>}
        </div>
        <div className={`p-3 rounded-lg border ${scheme.icon}`}>
          <Icon size={22} />
        </div>
      </div>
    </div>
  );
};
