import React from 'react';
import { ShieldAlert, Play, ChevronRight, CheckCircle2 } from 'lucide-react';
import { Threat } from '../../types';
import { SeverityBadge } from '../common/SeverityBadge';
import { StatusBadge } from '../common/StatusBadge';
import { ScoreBadge } from '../common/ScoreBadge';

interface InvestigationThreatsTabProps {
  threats: Threat[];
  runningDetection: boolean;
  onRunDetection: () => void;
  onSelectThreat: (threat: Threat) => void;
}

export const InvestigationThreatsTab: React.FC<InvestigationThreatsTabProps> = ({
  threats,
  runningDetection,
  onRunDetection,
  onSelectThreat,
}) => {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between pb-2">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <ShieldAlert className="text-red-400" size={18} />
            <span>Correlated Threat Detections ({threats.length})</span>
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Heuristic detections with exact evidence links and scoring formula transparency.
          </p>
        </div>
        <button
          onClick={onRunDetection}
          disabled={runningDetection}
          className="px-4 py-2 rounded-xl bg-red-600 hover:bg-red-500 text-white text-xs font-semibold flex items-center gap-2 transition-all cursor-pointer disabled:opacity-50"
        >
          <Play size={14} className={runningDetection ? 'animate-spin' : ''} />
          <span>Re-run Detection Engine</span>
        </button>
      </div>

      <div className="space-y-4">
        {threats.map((threat) => (
          <div
            key={threat.id}
            className="p-6 rounded-2xl bg-slate-900/90 border border-slate-800 hover:border-slate-700 shadow-xl transition-all space-y-4"
          >
            <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
              <div className="space-y-1.5">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="font-mono text-xs text-cyan-400 bg-cyan-950/60 px-2.5 py-0.5 rounded border border-cyan-500/30">
                    {threat.ruleCode}
                  </span>
                  <SeverityBadge severity={threat.severity} size="sm" />
                  <ScoreBadge score={threat.score} />
                  <StatusBadge status={threat.status} />
                </div>
                <h3 className="text-lg font-bold text-white">{threat.title}</h3>
                <p className="text-xs text-slate-300 leading-relaxed">{threat.description}</p>
              </div>

              <button
                onClick={() => onSelectThreat(threat)}
                className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-cyan-600 hover:text-white text-slate-200 text-xs font-semibold transition-all flex items-center gap-1.5 self-start sm:self-auto cursor-pointer shrink-0"
              >
                <span>Full Analysis & Evidence</span>
                <ChevronRight size={14} />
              </button>
            </div>

            {threat.escalationReason && (
              <div className="p-3.5 rounded-xl bg-red-950/40 border border-red-500/40 flex items-start gap-3">
                <ShieldAlert size={18} className="text-red-400 shrink-0 mt-0.5" />
                <div className="text-xs">
                  <span className="font-semibold text-red-300 block">Severity Escalation Override</span>
                  <span className="font-mono text-red-200">{threat.escalationReason}</span>
                  <div className="mt-1 text-[11px] text-red-300/80">
                    Detection Score: <span className="font-mono font-bold text-white">{threat.score}</span> &bull; Severity explicitly escalated to <span className="font-bold text-red-400">CRITICAL</span> without synthetic score inflation.
                  </div>
                </div>
              </div>
            )}

            {threat.scoreBreakdown && (
              <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800 text-xs font-mono text-slate-300 flex items-center justify-between">
                <span className="text-slate-500 uppercase text-[10px]">Scoring Formula</span>
                <span className="text-cyan-300 font-semibold">{threat.scoreBreakdown}</span>
              </div>
            )}

            <div className="flex items-center justify-between text-xs text-slate-400 pt-2 border-t border-slate-800/80">
              <span>Observed: <span className="font-mono text-slate-300">{threat.firstObserved} &rarr; {threat.lastObserved}</span></span>
              <span className="font-mono text-cyan-400">{threat.evidenceEvents?.length || 0} Linked Evidence Log Record(s)</span>
            </div>
          </div>
        ))}

        {threats.length === 0 && (
          <div className="p-12 text-center rounded-2xl bg-slate-900/60 border border-slate-800 space-y-3">
            <CheckCircle2 size={32} className="text-emerald-500 mx-auto" />
            <h3 className="text-base font-bold text-white">No Active Threats Detected</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              Ingest log files and trigger the correlation engine to discover brute-force sequences, account creations, or auth anomalies.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};
