import React, { useState } from 'react';
import {
  Clock,
  AlertTriangle,
  Eye,
  UserCheck,
  XCircle,
  Lock,
  UserPlus,
  Key,
  Shield
} from 'lucide-react';
import { TimelineEvent, LogEntry } from '../../types';

interface InvestigationTimelineTabProps {
  timeline: TimelineEvent[];
  onSelectLog: (log: LogEntry) => void;
}

export const InvestigationTimelineTab: React.FC<InvestigationTimelineTabProps> = ({
  timeline,
  onSelectLog,
}) => {
  const [suspiciousOnly, setSuspiciousOnly] = useState(false);

  const filteredTimeline = timeline.filter((event) => {
    if (suspiciousOnly && !event.suspicious) return false;
    return true;
  });

  const getEventIcon = (eventType: string, suspicious: boolean) => {
    if (suspicious) return AlertTriangle;
    switch (eventType) {
      case 'LOGIN_SUCCESS': return UserCheck;
      case 'LOGIN_FAILED': return XCircle;
      case 'ACCOUNT_LOCKED': return Lock;
      case 'USER_CREATED': return UserPlus;
      case 'PASSWORD_CHANGED': return Key;
      case 'PRIVILEGE_ESCALATED': return Shield;
      default: return Clock;
    }
  };

  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-sm space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Clock className="text-cyan-400" size={18} />
            <span>Chronological Forensic Timeline ({filteredTimeline.length} events)</span>
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Temporal sequencing reconstructed from raw timestamps to establish order of occurrence.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <label className="flex items-center gap-2 cursor-pointer text-xs font-mono text-slate-300 bg-slate-950 px-3 py-1.5 rounded-xl border border-slate-800">
            <input
              type="checkbox"
              checked={suspiciousOnly}
              onChange={(e) => setSuspiciousOnly(e.target.checked)}
              className="rounded text-cyan-500 focus:ring-0"
            />
            <span className="text-amber-400">Show Suspicious Events Only</span>
          </label>
        </div>
      </div>

      {filteredTimeline.length > 0 ? (
        <div className="relative pl-6 space-y-6 before:absolute before:left-3 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-800">
          {filteredTimeline.map((event, index) => {
            const Icon = getEventIcon(event.eventType, event.suspicious);
            return (
              <div key={event.id || index} className="relative group">
                <div
                  className={`absolute -left-6 top-1 w-6 h-6 rounded-full flex items-center justify-center border transition-all ${
                    event.suspicious
                      ? 'bg-red-950 border-red-500 text-red-400 shadow-[0_0_10px_rgba(239,68,68,0.5)] animate-pulse'
                      : 'bg-slate-900 border-slate-700 text-cyan-400 group-hover:border-cyan-400'
                  }`}
                >
                  <Icon size={12} />
                </div>

                <div
                  className={`p-4 rounded-xl border transition-all ${
                    event.suspicious
                      ? 'bg-red-950/20 border-red-500/40 shadow-[0_0_15px_rgba(239,68,68,0.05)]'
                      : 'bg-slate-950/60 border-slate-800/80 group-hover:border-slate-700'
                  }`}
                >
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-2">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-mono text-xs font-semibold text-white">
                        {event.timestamp}
                      </span>
                      <span className="font-mono text-[11px] bg-slate-800 text-cyan-300 px-2 py-0.5 rounded border border-slate-700">
                        {event.eventType}
                      </span>
                      {event.username && (
                        <span className="font-mono text-xs text-slate-300 bg-slate-900 px-2 py-0.5 rounded">
                          user: <span className="text-cyan-400">{event.username}</span>
                        </span>
                      )}
                      {event.suspicious && (
                        <span className="text-[10px] font-mono font-bold bg-red-900/60 text-red-300 border border-red-500/40 px-2 py-0.5 rounded-full flex items-center gap-1">
                          <AlertTriangle size={10} />
                          SUSPICIOUS ANOMALY
                        </span>
                      )}
                    </div>

                    <button
                      onClick={() => {
                        onSelectLog({
                          id: event.id,
                          investigationId: event.investigationId,
                          timestamp: event.timestamp,
                          eventType: event.eventType,
                          username: event.username,
                          source: event.source,
                          severity: event.severity,
                          description: event.description,
                          rawMessage: event.rawMessage,
                        });
                      }}
                      className="text-xs text-cyan-400 hover:text-cyan-300 font-mono flex items-center gap-1 self-start sm:self-auto cursor-pointer"
                    >
                      <Eye size={12} />
                      <span>View Verbatim Log</span>
                    </button>
                  </div>

                  <p className="text-xs text-slate-300 font-mono bg-black/40 p-2.5 rounded-lg border border-slate-800/60">
                    {event.rawMessage}
                  </p>

                  {event.associatedThreatTitles && event.associatedThreatTitles.length > 0 && (
                    <div className="mt-2 pt-2 border-t border-slate-800/80 flex items-center gap-2 flex-wrap">
                      <span className="text-[10px] uppercase font-semibold text-red-400 font-mono">Associated Threat Trigger:</span>
                      {event.associatedThreatTitles.map((title, idx) => (
                        <span key={idx} className="text-[11px] font-mono text-red-300 bg-red-950/40 border border-red-500/30 px-2 py-0.5 rounded">
                          {title}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="p-12 text-center text-xs text-slate-500 font-mono">
          No events found matching current timeline criteria.
        </div>
      )}
    </div>
  );
};
