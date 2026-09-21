import React from 'react';
import { Search } from 'lucide-react';
import { LogEntry } from '../../types';

interface InvestigationEventsTabProps {
  logs: LogEntry[];
  totalLogElements: number;
  totalLogPages: number;
  logPage: number;
  searchKeyword: string;
  selectedEventType: string;
  selectedUsername: string;
  onSearchChange: (keyword: string) => void;
  onEventTypeChange: (type: string) => void;
  onUsernameChange: (username: string) => void;
  onPageChange: (page: number) => void;
  onSelectLog: (log: LogEntry) => void;
}

export const InvestigationEventsTab: React.FC<InvestigationEventsTabProps> = ({
  logs,
  totalLogElements,
  totalLogPages,
  logPage,
  searchKeyword,
  selectedEventType,
  selectedUsername,
  onSearchChange,
  onEventTypeChange,
  onUsernameChange,
  onPageChange,
  onSelectLog,
}) => {
  return (
    <div className="space-y-4">
      <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 shadow-lg flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="relative w-full md:w-80">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
            <Search size={14} />
          </div>
          <input
            type="text"
            value={searchKeyword}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Filter by raw log keyword..."
            className="w-full bg-slate-950 border border-slate-700 rounded-xl pl-9 pr-3 py-1.5 text-xs text-white placeholder-slate-500 font-mono focus:border-cyan-400 focus:outline-none"
          />
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto text-xs">
          <select
            value={selectedEventType}
            onChange={(e) => onEventTypeChange(e.target.value)}
            className="bg-slate-950 border border-slate-700 rounded-xl px-3 py-1.5 text-slate-300 font-mono outline-none"
          >
            <option value="">ALL EVENT TYPES</option>
            <option value="LOGIN_SUCCESS">LOGIN_SUCCESS</option>
            <option value="LOGIN_FAILED">LOGIN_FAILED</option>
            <option value="ACCOUNT_LOCKED">ACCOUNT_LOCKED</option>
            <option value="USER_CREATED">USER_CREATED</option>
            <option value="PASSWORD_CHANGED">PASSWORD_CHANGED</option>
            <option value="PRIVILEGE_ESCALATED">PRIVILEGE_ESCALATED</option>
          </select>

          <input
            type="text"
            value={selectedUsername}
            onChange={(e) => onUsernameChange(e.target.value)}
            placeholder="Username..."
            className="bg-slate-950 border border-slate-700 rounded-xl px-3 py-1.5 text-slate-300 font-mono placeholder-slate-500 w-32 outline-none"
          />
        </div>
      </div>

      <div className="bg-slate-900/90 border border-slate-800 rounded-2xl shadow-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-slate-800 bg-slate-950/60 font-mono text-[11px] uppercase tracking-wider text-slate-400">
                <th className="px-5 py-3.5">Event ID</th>
                <th className="px-5 py-3.5">Timestamp</th>
                <th className="px-5 py-3.5">Event Type</th>
                <th className="px-5 py-3.5">Subject User</th>
                <th className="px-5 py-3.5">Verbatim Message</th>
                <th className="px-5 py-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-slate-300 font-sans">
              {logs.map((log) => (
                <tr key={log.id} className="hover:bg-slate-800/40 transition-colors">
                  <td className="px-5 py-3 font-mono text-slate-500 text-[11px]">
                    #{log.id}
                  </td>
                  <td className="px-5 py-3 font-mono text-slate-300 whitespace-nowrap">
                    {log.timestamp}
                  </td>
                  <td className="px-5 py-3 whitespace-nowrap">
                    <span className="font-mono text-[11px] bg-slate-950 text-cyan-300 px-2 py-0.5 rounded border border-slate-800">
                      {log.eventType}
                    </span>
                  </td>
                  <td className="px-5 py-3 font-mono text-white whitespace-nowrap">
                    {log.username || '-'}
                  </td>
                  <td className="px-5 py-3 font-mono text-slate-400 text-[11px] truncate max-w-md">
                    {log.rawMessage}
                  </td>
                  <td className="px-5 py-3 text-right whitespace-nowrap">
                    <button
                      onClick={() => onSelectLog(log)}
                      className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-cyan-400 font-mono text-[11px] transition-colors cursor-pointer"
                    >
                      Inspect
                    </button>
                  </td>
                </tr>
              ))}
              {logs.length === 0 && (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-slate-500 font-mono text-xs">
                    No log entries matched your filter parameters.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="px-5 py-3 border-t border-slate-800 bg-slate-950/50 flex items-center justify-between text-xs font-mono text-slate-400">
          <span>Showing {logs.length} of {totalLogElements} events</span>
          <div className="flex items-center gap-2">
            <button
              disabled={logPage === 0}
              onClick={() => onPageChange(Math.max(0, logPage - 1))}
              className="px-3 py-1 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 cursor-pointer"
            >
              Previous
            </button>
            <span>Page {logPage + 1} of {Math.max(1, totalLogPages)}</span>
            <button
              disabled={logPage + 1 >= totalLogPages}
              onClick={() => onPageChange(logPage + 1)}
              className="px-3 py-1 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 cursor-pointer"
            >
              Next
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
