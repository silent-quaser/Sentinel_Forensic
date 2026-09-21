import React from 'react';
import { X, Terminal, Copy, Check } from 'lucide-react';
import { LogEntry } from '../../types';

interface RawLogModalProps {
  log: LogEntry | null;
  onClose: () => void;
}

export const RawLogModal: React.FC<RawLogModalProps> = ({ log, onClose }) => {
  const [copied, setCopied] = React.useState(false);

  if (!log) return null;

  const copyToClipboard = () => {
    navigator.clipboard.writeText(log.rawMessage);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
      <div className="bg-slate-900 border border-slate-700 rounded-xl w-full max-w-2xl overflow-hidden shadow-2xl animate-in fade-in zoom-in duration-150">
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-800 bg-slate-950/50">
          <div className="flex items-center gap-2 text-cyan-400 font-mono text-sm font-semibold">
            <Terminal size={18} />
            <span>RAW FORENSIC LOG ENTRY [ID: #{log.id}]</span>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded hover:bg-slate-800 transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        <div className="p-5 space-y-4">
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
            <div className="bg-slate-950/60 p-2.5 rounded border border-slate-800">
              <span className="text-slate-500 block">Timestamp</span>
              <span className="text-slate-200 font-mono">{log.timestamp}</span>
            </div>
            <div className="bg-slate-950/60 p-2.5 rounded border border-slate-800">
              <span className="text-slate-500 block">Event Type</span>
              <span className="text-cyan-400 font-mono font-medium">{log.eventType}</span>
            </div>
            <div className="bg-slate-950/60 p-2.5 rounded border border-slate-800">
              <span className="text-slate-500 block">Principal User</span>
              <span className="text-slate-200 font-mono">{log.username || 'N/A'}</span>
            </div>
            <div className="bg-slate-950/60 p-2.5 rounded border border-slate-800">
              <span className="text-slate-500 block">Source System</span>
              <span className="text-slate-200 font-mono">{log.source || 'N/A'}</span>
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-1.5">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Verbatim Log Record</span>
              <button
                onClick={copyToClipboard}
                className="flex items-center gap-1 text-xs text-cyan-400 hover:text-cyan-300 font-mono transition-colors"
              >
                {copied ? <Check size={14} /> : <Copy size={14} />}
                {copied ? 'Copied' : 'Copy Record'}
              </button>
            </div>
            <pre className="bg-black/90 p-4 rounded-lg border border-slate-800 text-xs font-mono text-emerald-400 overflow-x-auto whitespace-pre-wrap select-all">
              {log.rawMessage}
            </pre>
          </div>

          {log.metadata && Object.keys(log.metadata).length > 0 && (
            <div>
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5 block">Parsed Metadata</span>
              <pre className="bg-slate-950 p-3 rounded-lg border border-slate-800 text-xs font-mono text-slate-300 overflow-x-auto">
                {JSON.stringify(log.metadata, null, 2)}
              </pre>
            </div>
          )}
        </div>

        <div className="px-5 py-3 border-t border-slate-800 bg-slate-950/50 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-1.5 text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 rounded transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
