import React from 'react';

interface StatusBadgeProps {
  status: string;
  className?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, className = '' }) => {
  const s = status?.toUpperCase() || 'UNKNOWN';

  let colorClasses = 'bg-slate-800 text-slate-300 border-slate-700';

  switch (s) {
    case 'OPEN':
      colorClasses = 'bg-blue-950/60 text-blue-400 border-blue-500/40';
      break;
    case 'IN_PROGRESS':
      colorClasses = 'bg-cyan-950/60 text-cyan-400 border-cyan-500/40';
      break;
    case 'CLOSED':
    case 'RESOLVED':
      colorClasses = 'bg-emerald-950/60 text-emerald-400 border-emerald-500/40';
      break;
    case 'ARCHIVED':
      colorClasses = 'bg-slate-900 text-slate-400 border-slate-700';
      break;
    case 'DETECTED':
      colorClasses = 'bg-red-950/60 text-red-400 border-red-500/40';
      break;
    case 'REVIEWED':
      colorClasses = 'bg-amber-950/60 text-amber-300 border-amber-500/40';
      break;
    case 'CONFIRMED':
      colorClasses = 'bg-purple-950/60 text-purple-400 border-purple-500/40';
      break;
    case 'FALSE_POSITIVE':
      colorClasses = 'bg-slate-800 text-slate-400 border-slate-600';
      break;
  }

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${colorClasses} ${className}`}>
      {s.replace('_', ' ')}
    </span>
  );
};
