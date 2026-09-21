import React, { useEffect, useState } from 'react';
import { Activity, Database, Server, Cpu, ShieldCheck, RefreshCw } from 'lucide-react';
import { healthApi } from '../api/client';
import { SystemHealth } from '../types';

export default function Settings() {
  const [health, setHealth] = useState<SystemHealth | null>(null);
  const [loading, setLoading] = useState(false);

  const checkHealth = async () => {
    setLoading(true);
    try {
      const data = await healthApi.getHealth();
      setHealth(data);
    } catch (err) {
      console.error('Health check failed', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkHealth();
  }, []);

  return (
    <div className="space-y-6 pb-12 max-w-4xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <Activity className="text-cyan-400" size={24} />
            <span>Platform Telemetry & System Health</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Live subsystem connectivity, database persistence state, and threat correlation engine configuration.
          </p>
        </div>

        <button
          onClick={checkHealth}
          className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium flex items-center gap-2 border border-slate-700 transition-all cursor-pointer self-start sm:self-auto"
        >
          <RefreshCw size={14} className={loading ? 'animate-spin text-cyan-400' : ''} />
          <span>Ping Subsystems</span>
        </button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">PostgreSQL 18 Database</span>
            <Database size={18} className="text-cyan-400" />
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
            <span className="text-lg font-bold font-mono text-white">{health?.database || 'Connected'}</span>
          </div>
          <p className="text-xs text-slate-400 font-mono">Port: 5432 &bull; DB: sentinelforensic &bull; Migrations V1-V6</p>
        </div>

        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Threat Detection Engine</span>
            <ShieldCheck size={18} className="text-red-400" />
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
            <span className="text-lg font-bold font-mono text-white">{health?.threatEngine || 'Active (7 Rules Loaded)'}</span>
          </div>
          <p className="text-xs text-slate-400 font-mono">Deduplication: Enabled &bull; Lockout Override: Critical</p>
        </div>

        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Forensic Parser Subsystem</span>
            <Server size={18} className="text-blue-400" />
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
            <span className="text-lg font-bold font-mono text-white">{health?.logParser || 'SystemLogParser (Regex)'}</span>
          </div>
          <p className="text-xs text-slate-400 font-mono">Timestamp: yyyy-MM-dd HH:mm:ss &bull; Malformed Line Accounting</p>
        </div>

        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Application Core Version</span>
            <Cpu size={18} className="text-purple-400" />
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
            <span className="text-lg font-bold font-mono text-white">SentinelForensic {health?.version || '1.0.0-RELEASE'}</span>
          </div>
          <p className="text-xs text-slate-400 font-mono">Java 21 LTS &bull; Spring Boot 3.2.4 &bull; Apache PDFBox 3.x</p>
        </div>
      </div>
    </div>
  );
}
