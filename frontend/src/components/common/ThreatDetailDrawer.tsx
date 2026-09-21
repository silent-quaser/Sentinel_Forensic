import React, { useState } from 'react';
import { X, ShieldAlert, Clock, User, Globe, FileText, ChevronRight } from 'lucide-react';
import { Threat, LogEntry } from '../../types';
import { SeverityBadge } from './SeverityBadge';
import { StatusBadge } from './StatusBadge';
import { ScoreBadge } from './ScoreBadge';
import { RawLogModal } from './RawLogModal';
import { threatApi } from '../../api/client';

interface ThreatDetailDrawerProps {
  threat: Threat | null;
  onClose: () => void;
  onStatusUpdated?: (updatedThreat: Threat) => void;
}

export const ThreatDetailDrawer: React.FC<ThreatDetailDrawerProps> = ({
  threat,
  onClose,
  onStatusUpdated,
}) => {
  const [selectedStatus, setSelectedStatus] = useState<string>(threat?.status || 'DETECTED');
  const [savingStatus, setSavingStatus] = useState(false);
  const [selectedLog, setSelectedLog] = useState<LogEntry | null>(null);

  if (!threat) return null;

  const handleStatusChange = async (newStatus: string) => {
    setSelectedStatus(newStatus);
    setSavingStatus(true);
    try {
      const updated = await threatApi.updateStatus(threat.id, newStatus);
      if (onStatusUpdated) {
        onStatusUpdated(updated);
      }
    } catch (err) {
      console.error('Failed to update status', err);
    } finally {
      setSavingStatus(false);
    }
  };

  return (
    <>
      <div className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm transition-opacity" onClick={onClose} />
      <div className="fixed inset-y-0 right-0 z-50 w-full max-w-2xl bg-slate-900 border-l border-slate-800 shadow-2xl flex flex-col transform transition-transform duration-300 ease-in-out">
        <div className="p-6 border-b border-slate-800 bg-slate-950/60 flex items-start justify-between">
          <div className="space-y-1.5 pr-4">
            <div className="flex items-center gap-2 flex-wrap">
              <span className="font-mono text-xs text-cyan-400 bg-cyan-950/40 px-2 py-0.5 rounded border border-cyan-500/30">
                {threat.ruleCode}
              </span>
              <SeverityBadge severity={threat.severity} size="sm" />
              <ScoreBadge score={threat.score} />
              <StatusBadge status={selectedStatus} />
            </div>
            <h2 className="text-xl font-bold text-white leading-tight">{threat.title}</h2>
            <p className="text-xs text-slate-400">Detected on {threat.detectedAt}</p>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-2 rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {threat.escalationReason && (
            <div className="bg-red-950/40 border border-red-500/40 rounded-xl p-4 flex items-start gap-3 shadow-[0_0_15px_rgba(239,68,68,0.15)]">
              <ShieldAlert className="text-red-400 shrink-0 mt-0.5" size={20} />
              <div>
                <h4 className="text-sm font-semibold text-red-300">Severity Escalation Override</h4>
                <p className="text-xs text-red-200/90 mt-1 font-mono">
                  {threat.escalationReason}
                </p>
                <div className="mt-2 text-[11px] text-red-300/70">
                  Calculated Detection Score: <span className="font-mono font-bold text-white">{threat.score}</span> (Confirmed override to <span className="font-bold text-red-400">CRITICAL</span> without synthetic score inflation)
                </div>
              </div>
            </div>
          )}

          <div className="grid grid-cols-2 gap-3 text-xs">
            <div className="bg-slate-950/50 p-3 rounded-lg border border-slate-800">
              <div className="flex items-center gap-1.5 text-slate-400 mb-1">
                <User size={14} className="text-cyan-400" />
                <span>Target Subject</span>
              </div>
              <span className="font-mono font-medium text-white text-sm">
                {threat.affectedUser || 'Not Specified'}
              </span>
            </div>
            <div className="bg-slate-950/50 p-3 rounded-lg border border-slate-800">
              <div className="flex items-center gap-1.5 text-slate-400 mb-1">
                <Globe size={14} className="text-cyan-400" />
                <span>Source IP / Origin</span>
              </div>
              <span className="font-mono font-medium text-white text-sm">
                {threat.affectedIp || 'Internal / Local'}
              </span>
            </div>
            <div className="bg-slate-950/50 p-3 rounded-lg border border-slate-800 col-span-2">
              <div className="flex items-center gap-1.5 text-slate-400 mb-1">
                <Clock size={14} className="text-cyan-400" />
                <span>Observation Window</span>
              </div>
              <span className="font-mono text-slate-200">
                {threat.firstObserved} &rarr; {threat.lastObserved}
              </span>
            </div>
          </div>

          <div className="space-y-3">
            <div>
              <h4 className="text-xs uppercase font-semibold tracking-wider text-slate-400 mb-1.5">Threat Summary</h4>
              <p className="text-sm text-slate-200 leading-relaxed bg-slate-950/40 p-3.5 rounded-lg border border-slate-800/80">
                {threat.description}
              </p>
            </div>

            {threat.explanation && (
              <div>
                <h4 className="text-xs uppercase font-semibold tracking-wider text-slate-400 mb-1.5">Forensic Rule Logic</h4>
                <div className="bg-slate-950/40 p-3.5 rounded-lg border border-slate-800/80 text-xs text-slate-300 space-y-2">
                  <p>{threat.explanation}</p>
                  {threat.scoreBreakdown && (
                    <div className="pt-2 border-t border-slate-800/80 font-mono text-[11px] text-cyan-300">
                      <span className="text-slate-500 block text-[10px] uppercase font-sans mb-0.5">Score Breakdown Formula</span>
                      {threat.scoreBreakdown}
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <h4 className="text-xs uppercase font-semibold tracking-wider text-slate-400 flex items-center gap-1.5">
                <FileText size={14} className="text-cyan-400" />
                <span>Causative Evidence Events ({threat.evidenceEvents?.length || 0})</span>
              </h4>
              <span className="text-[11px] text-slate-500">Click event to inspect verbatim log</span>
            </div>

            <div className="space-y-2">
              {threat.evidenceEvents && threat.evidenceEvents.length > 0 ? (
                threat.evidenceEvents.map((ev, index) => (
                  <div
                    key={ev.id || index}
                    onClick={() => setSelectedLog(ev)}
                    className="p-3 rounded-lg bg-slate-950/60 border border-slate-800 hover:border-cyan-500/50 hover:bg-slate-800/40 transition-all cursor-pointer flex items-center justify-between group"
                  >
                    <div className="space-y-1 pr-3">
                      <div className="flex items-center gap-2">
                        <span className="text-[10px] font-mono bg-slate-800 text-slate-300 px-1.5 py-0.5 rounded">
                          #{ev.id}
                        </span>
                        <span className="text-xs font-mono font-medium text-cyan-300">
                          {ev.eventType}
                        </span>
                        <span className="text-xs text-slate-400 font-mono">
                          {ev.timestamp}
                        </span>
                      </div>
                      <p className="text-xs text-slate-300 font-mono truncate max-w-md">
                        {ev.rawMessage}
                      </p>
                    </div>
                    <ChevronRight size={16} className="text-slate-600 group-hover:text-cyan-400 transition-colors shrink-0" />
                  </div>
                ))
              ) : (
                <div className="p-4 text-center rounded-lg bg-slate-950/40 border border-slate-800 text-slate-500 text-xs">
                  No specific evidence events recorded.
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="p-5 border-t border-slate-800 bg-slate-950/80 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-400 font-medium">Investigator Status:</span>
            <select
              value={selectedStatus}
              disabled={savingStatus}
              onChange={(e) => handleStatusChange(e.target.value)}
              className="bg-slate-800 text-white border border-slate-700 rounded-lg px-3 py-1.5 text-xs font-medium focus:ring-1 focus:ring-cyan-400 outline-none cursor-pointer disabled:opacity-50"
            >
              <option value="DETECTED">DETECTED</option>
              <option value="REVIEWED">REVIEWED</option>
              <option value="CONFIRMED">CONFIRMED</option>
              <option value="FALSE_POSITIVE">FALSE POSITIVE</option>
              <option value="RESOLVED">RESOLVED</option>
            </select>
          </div>

          <button
            onClick={onClose}
            className="px-4 py-1.5 text-xs font-medium bg-slate-800 hover:bg-slate-700 text-white rounded-lg transition-colors"
          >
            Close
          </button>
        </div>
      </div>

      {selectedLog && (
        <RawLogModal log={selectedLog} onClose={() => setSelectedLog(null)} />
      )}
    </>
  );
};
