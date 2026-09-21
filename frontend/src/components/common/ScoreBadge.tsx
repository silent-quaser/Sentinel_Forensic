import React from 'react';

interface ScoreBadgeProps {
  score: number;
  className?: string;
}

export const ScoreBadge: React.FC<ScoreBadgeProps> = ({ score, className = '' }) => {
  let color = 'text-cyan-400 border-cyan-500/30 bg-cyan-950/30';
  if (score >= 75) {
    color = 'text-red-400 border-red-500/40 bg-red-950/40 shadow-[0_0_10px_rgba(239,68,68,0.25)]';
  } else if (score >= 50) {
    color = 'text-orange-400 border-orange-500/40 bg-orange-950/40';
  } else if (score >= 25) {
    color = 'text-amber-300 border-amber-500/40 bg-amber-950/40';
  }

  return (
    <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-md text-xs font-mono font-semibold border ${color} ${className}`}>
      <span className="text-slate-400 font-normal">Score:</span>
      <span>{score}</span>
    </span>
  );
};
