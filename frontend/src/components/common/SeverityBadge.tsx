import React from 'react';
import { ThreatSeverity } from '../../types';

interface SeverityBadgeProps {
  severity: ThreatSeverity | string;
  className?: string;
  size?: 'sm' | 'md' | 'lg';
}

export const SeverityBadge: React.FC<SeverityBadgeProps> = ({ severity, className = '', size = 'md' }) => {
  const s = severity?.toUpperCase() || 'LOW';

  let bg = 'bg-slate-800/80 text-slate-300 border-slate-700';
  let dot = 'bg-slate-400';

  if (s === 'CRITICAL') {
    bg = 'bg-red-950/60 text-red-400 border-red-500/40 shadow-[0_0_10px_rgba(239,68,68,0.2)]';
    dot = 'bg-red-500 animate-pulse';
  } else if (s === 'HIGH') {
    bg = 'bg-orange-950/60 text-orange-400 border-orange-500/40';
    dot = 'bg-orange-500';
  } else if (s === 'MEDIUM') {
    bg = 'bg-amber-950/60 text-amber-300 border-amber-500/40';
    dot = 'bg-amber-400';
  } else if (s === 'LOW') {
    bg = 'bg-cyan-950/60 text-cyan-400 border-cyan-500/40';
    dot = 'bg-cyan-400';
  }

  const sizeClasses = size === 'sm' ? 'px-2 py-0.5 text-xs' : size === 'lg' ? 'px-3 py-1.5 text-sm font-semibold' : 'px-2.5 py-1 text-xs font-medium';

  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full border ${bg} ${sizeClasses} ${className}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${dot}`} />
      {s}
    </span>
  );
};
