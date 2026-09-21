import React from 'react';
import { ChevronRight } from 'lucide-react';
import { Investigation, Threat, TimelineEvent } from '../../types';
import { SeverityBadge } from '../common/SeverityBadge';
import { ScoreBadge } from '../common/ScoreBadge';

interface InvestigationOverviewTabProps {
  investigation: Investigation;
  threats: Threat[];
  timeline: TimelineEvent[];
  onSelectThreat: (threat: Threat) => void;
  onRunDetection: () => void;
}

export const InvestigationOverviewTab: React.FC<InvestigationOverviewTabProps> = ({
  investigation,
  threats,
  timeline,
  onSelectThreat,
}) => {
  const criticalThreatCount = threats.filter((t) => t.severity === 'CRITICAL').length;
  const highThreatCount = threats.filter((t) => t.severity === 'HIGH').length;

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-5 shadow">
          <span className="text-xs uppercase font-semibold text-slate-400 block mb-1">Total Ingested Events</span>
          <p className="text-3xl font-bold font-mono text-white">{timeline.length}</p>
          <p className="text-xs text-cyan-400 mt-1 font-mono">Chronologically parsed</p>
        </div>
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-5 shadow">
          <span className="text-xs uppercase font-semibold text-slate-400 block mb-1">Correlated Threats</span>
          <p className="text-3xl font-bold font-mono text-white">{threats.length}</p>
          <p className="text-xs text-amber-400 mt-1 font-mono">7 rules evaluated</p>
        </div>
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-5 shadow">
          <span className="text-xs uppercase font-semibold text-slate-400 block mb-1">Critical Severity Alerts</span>
          <p className="text-3xl font-bold font-mono text-red-400">{criticalThreatCount}</p>
          <p className="text-xs text-slate-400 mt-1 font-mono">Immediate escalation</p>
        </div>
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-5 shadow">
          <span className="text-xs uppercase font-semibold text-slate-400 block mb-1">High Severity Alerts</span>
          <p className="text-3xl font-bold font-mono text-orange-400">{highThreatCount}</p>
          <p className="text-xs text-slate-400 mt-1 font-mono">Privilege / burst anomalies</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-lg space-y-4">
          <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300">
            Incident Scope & Technical Narrative
          </h3>
          <p className="text-xs text-slate-300 leading-relaxed bg-slate-950/50 p-4 rounded-xl border border-slate-800">
            {investigation.description || 'No detailed technical narrative registered for this case dossier.'}
          </p>

          <div>
            <h4 className="text-xs uppercase font-semibold tracking-wider text-slate-400 mb-2">
              Threat Detections in this Dossier
            </h4>
            <div className="space-y-2">
              {threats.map((t) => (
                <div
                  key={t.id}
                  onClick={() => onSelectThreat(t)}
                  className="p-3 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-cyan-500/40 transition-all flex items-center justify-between cursor-pointer group"
                >
                  <div className="space-y-0.5 pr-2 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-[11px] font-mono text-cyan-400 bg-cyan-950/40 px-1.5 py-0.5 rounded">{t.ruleCode}</span>
                      <SeverityBadge severity={t.severity} size="sm" />
                      <ScoreBadge score={t.score} />
                    </div>
                    <p className="text-xs font-semibold text-white group-hover:text-cyan-300 truncate">{t.title}</p>
                    {t.escalationReason && (
                      <p className="text-[11px] text-red-300 font-mono">Override: {t.escalationReason}</p>
                    )}
                  </div>
                  <ChevronRight size={16} className="text-slate-600 group-hover:text-cyan-400 shrink-0" />
                </div>
              ))}
              {threats.length === 0 && (
                <div className="p-6 text-center text-xs text-slate-500 bg-slate-950/40 rounded-xl border border-slate-800">
                  No threats detected. Click "Run Detection" to correlate ingested log lines against the 7 threat rules.
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-lg space-y-4">
          <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300">
            Dossier Metadata
          </h3>
          <div className="space-y-3 text-xs">
            <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-slate-500 block text-[11px]">Case Identifier</span>
              <span className="font-mono text-cyan-400 font-semibold">{investigation.investigationId}</span>
            </div>
            <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-slate-500 block text-[11px]">Lead Investigator</span>
              <span className="font-mono text-white font-medium">{investigation.investigatorName}</span>
            </div>
            <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-slate-500 block text-[11px]">Dossier Created</span>
              <span className="font-mono text-slate-300">{investigation.createdAt}</span>
            </div>
            <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-slate-500 block text-[11px]">Detection Engine</span>
              <span className="font-mono text-emerald-400">PostgreSQL 18 + SentinelRules v1.0</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
