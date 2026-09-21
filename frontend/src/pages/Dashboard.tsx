import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  FolderLock,
  FileText,
  AlertTriangle,
  ShieldAlert,
  Users,
  ArrowUpRight,
  RefreshCw,
  PlusCircle
} from 'lucide-react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip as RechartsTooltip,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid
} from 'recharts';
import { dashboardApi } from '../api/client';
import { DashboardSummary, Threat } from '../types';
import { StatCard } from '../components/common/StatCard';
import { SeverityBadge } from '../components/common/SeverityBadge';
import { StatusBadge } from '../components/common/StatusBadge';
import { ScoreBadge } from '../components/common/ScoreBadge';
import { ThreatDetailDrawer } from '../components/common/ThreatDetailDrawer';

export default function Dashboard() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [selectedThreat, setSelectedThreat] = useState<Threat | null>(null);

  const loadData = async () => {
    try {
      const data = await dashboardApi.getSummary();
      setSummary(data);
    } catch (err) {
      console.error('Failed to load dashboard summary', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleRefresh = () => {
    setRefreshing(true);
    loadData();
  };

  const SEVERITY_COLORS: Record<string, string> = {
    CRITICAL: '#ef4444',
    HIGH: '#f97316',
    MEDIUM: '#f59e0b',
    LOW: '#06b6d4',
  };

  const severityPieData = summary?.threatSeverityDistribution
    ? Object.entries(summary.threatSeverityDistribution).map(([name, value]) => ({
        name,
        value: Number(value),
      }))
    : [];

  const activityData = summary?.activitySeries || [];

  return (
    <div className="space-y-6 pb-12">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900/60 p-6 rounded-2xl border border-slate-800 backdrop-blur-sm">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="flex h-2.5 w-2.5 relative">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-cyan-500"></span>
            </span>
            <span className="text-xs font-mono font-semibold uppercase tracking-wider text-cyan-400">
              Operational Threat Intelligence Center
            </span>
          </div>
          <h1 className="text-2xl lg:text-3xl font-bold text-white tracking-tight">
            Incident Triage & Forensic Analysis
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Real-time correlation across authentication attempts, access control, and anomaly streams.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleRefresh}
            disabled={refreshing}
            className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium flex items-center gap-2 border border-slate-700 transition-all cursor-pointer disabled:opacity-50"
          >
            <RefreshCw size={14} className={refreshing ? 'animate-spin text-cyan-400' : ''} />
            <span>Refresh Telemetry</span>
          </button>
          <Link
            to="/investigations/new"
            className="px-4 py-2 rounded-xl bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white text-xs font-semibold flex items-center gap-2 shadow-[0_0_15px_rgba(6,182,212,0.3)] transition-all"
          >
            <PlusCircle size={15} />
            <span>Create Case</span>
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        <StatCard
          title="Active Investigations"
          value={summary?.activeInvestigations ?? 0}
          subtitle="Open / In-Progress Dossiers"
          icon={FolderLock}
          color="blue"
        />
        <StatCard
          title="Total Events Ingested"
          value={summary?.totalEvents ?? 0}
          subtitle="Parsed forensic records"
          icon={FileText}
          color="cyan"
        />
        <StatCard
          title="Detected Threats"
          value={summary?.detectedThreats ?? 0}
          subtitle="Rule-based correlation matches"
          icon={AlertTriangle}
          color="amber"
        />
        <StatCard
          title="Critical / High Alerts"
          value={summary?.highCriticalThreats ?? 0}
          subtitle="Immediate response required"
          icon={ShieldAlert}
          color="red"
        />
        <StatCard
          title="Monitored Subjects"
          value={summary?.distinctUsers ?? 0}
          subtitle="Unique principals tracked"
          icon={Users}
          color="purple"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-lg backdrop-blur-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300">
                Threat Severity Matrix
              </h3>
              <span className="text-[11px] font-mono text-cyan-400 bg-cyan-950/40 px-2 py-0.5 rounded border border-cyan-500/20">
                DISTRIBUTION
              </span>
            </div>
            <p className="text-xs text-slate-400 mb-4">
              Classification breakdown of active correlation rule triggers.
            </p>
          </div>

          <div className="h-56 w-full flex items-center justify-center relative">
            {severityPieData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={severityPieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={80}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {severityPieData.map((entry) => (
                      <Cell
                        key={`cell-${entry.name}`}
                        fill={SEVERITY_COLORS[entry.name] || '#64748b'}
                      />
                    ))}
                  </Pie>
                  <RechartsTooltip
                    contentStyle={{
                      backgroundColor: '#0f172a',
                      borderColor: '#334155',
                      borderRadius: '8px',
                      fontSize: '12px',
                      color: '#f8fafc',
                    }}
                  />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="text-center text-xs text-slate-500">
                No threats detected yet across investigations.
              </div>
            )}
          </div>

          <div className="grid grid-cols-2 gap-2 pt-4 border-t border-slate-800/80 text-xs">
            {['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].map((sev) => {
              const val = summary?.threatSeverityDistribution?.[sev] || 0;
              return (
                <div key={sev} className="flex items-center justify-between p-2 rounded bg-slate-950/50 border border-slate-800">
                  <div className="flex items-center gap-1.5">
                    <span
                      className="w-2 h-2 rounded-full"
                      style={{ backgroundColor: SEVERITY_COLORS[sev] }}
                    />
                    <span className="text-slate-400 text-[11px]">{sev}</span>
                  </div>
                  <span className="font-mono font-bold text-white">{val}</span>
                </div>
              );
            })}
          </div>
        </div>

        <div className="lg:col-span-2 bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-lg backdrop-blur-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300">
                Forensic Activity & Telemetry Velocity
              </h3>
              <span className="text-[11px] font-mono text-cyan-400 bg-cyan-950/40 px-2 py-0.5 rounded border border-cyan-500/20">
                SERIES
              </span>
            </div>
            <p className="text-xs text-slate-400 mb-4">
              Chronological log ingest volume correlated against threat triggers.
            </p>
          </div>

          <div className="h-64 w-full">
            {activityData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={activityData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorEvents" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#06b6d4" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#06b6d4" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="colorThreats" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#ef4444" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#ef4444" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                  <XAxis dataKey="timestamp" stroke="#64748b" fontSize={10} />
                  <YAxis stroke="#64748b" fontSize={10} />
                  <RechartsTooltip
                    contentStyle={{
                      backgroundColor: '#0f172a',
                      borderColor: '#334155',
                      borderRadius: '8px',
                      fontSize: '12px',
                    }}
                  />
                  <Area
                    type="monotone"
                    dataKey="eventCount"
                    stroke="#06b6d4"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#colorEvents)"
                    name="Log Events"
                  />
                  <Area
                    type="monotone"
                    dataKey="threatCount"
                    stroke="#ef4444"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#colorThreats)"
                    name="Threat Detections"
                  />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-xs text-slate-500">
                Ingest logs to visualize forensic activity velocity.
              </div>
            )}
          </div>

          <div className="flex items-center gap-6 pt-4 border-t border-slate-800/80 text-xs text-slate-400">
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded bg-cyan-500/30 border border-cyan-400" />
              <span>Log Event Count</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded bg-red-500/30 border border-red-400" />
              <span>Correlated Threats Triggered</span>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-lg backdrop-blur-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300">
                Active Investigations
              </h3>
              <p className="text-xs text-slate-400 mt-0.5">Current case files underway</p>
            </div>
            <Link
              to="/investigations"
              className="text-xs text-cyan-400 hover:text-cyan-300 flex items-center gap-1 font-mono transition-colors"
            >
              <span>View All</span>
              <ArrowUpRight size={14} />
            </Link>
          </div>

          <div className="space-y-3">
            {summary?.recentInvestigations && summary.recentInvestigations.length > 0 ? (
              summary.recentInvestigations.map((inv) => (
                <Link
                  key={inv.id}
                  to={`/investigations/${inv.id}`}
                  className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80 hover:border-cyan-500/40 hover:bg-slate-800/30 transition-all flex items-center justify-between group block"
                >
                  <div className="space-y-1 min-w-0 pr-3">
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-xs text-cyan-400">
                        {inv.investigationId}
                      </span>
                      <StatusBadge status={inv.status} />
                    </div>
                    <h4 className="text-sm font-semibold text-white group-hover:text-cyan-300 transition-colors truncate">
                      {inv.name}
                    </h4>
                    <p className="text-xs text-slate-400 truncate">
                      Lead: {inv.investigatorName} &bull; {inv.createdAt ? new Date(inv.createdAt).toLocaleDateString() : ''}
                    </p>
                  </div>
                  <ArrowUpRight size={16} className="text-slate-600 group-hover:text-cyan-400 transition-colors shrink-0" />
                </Link>
              ))
            ) : (
              <div className="p-8 text-center rounded-xl bg-slate-950/40 border border-slate-800/60 text-slate-500 text-xs">
                No active investigations recorded. Create your first case above.
              </div>
            )}
          </div>
        </div>

        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-lg backdrop-blur-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300">
                Recent Threat Detections
              </h3>
              <p className="text-xs text-slate-400 mt-0.5">Latest correlation engine matches</p>
            </div>
            <Link
              to="/threats"
              className="text-xs text-cyan-400 hover:text-cyan-300 flex items-center gap-1 font-mono transition-colors"
            >
              <span>Threat Center</span>
              <ArrowUpRight size={14} />
            </Link>
          </div>

          <div className="space-y-3">
            {summary?.recentThreats && summary.recentThreats.length > 0 ? (
              summary.recentThreats.map((threat) => (
                <div
                  key={threat.id}
                  onClick={() => setSelectedThreat(threat)}
                  className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80 hover:border-red-500/40 hover:bg-slate-800/30 transition-all flex items-center justify-between cursor-pointer group"
                >
                  <div className="space-y-1 min-w-0 pr-3">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-mono text-[11px] text-cyan-400 bg-cyan-950/40 px-1.5 py-0.5 rounded border border-cyan-500/20">
                        {threat.ruleCode}
                      </span>
                      <SeverityBadge severity={threat.severity} size="sm" />
                      <ScoreBadge score={threat.score} />
                    </div>
                    <h4 className="text-sm font-semibold text-white group-hover:text-red-300 transition-colors truncate">
                      {threat.title}
                    </h4>
                    <p className="text-xs text-slate-400 truncate">
                      User: <span className="font-mono text-slate-300">{threat.affectedUser || 'Unknown'}</span> &bull; {threat.detectedAt}
                    </p>
                  </div>
                  <ArrowUpRight size={16} className="text-slate-600 group-hover:text-red-400 transition-colors shrink-0" />
                </div>
              ))
            ) : (
              <div className="p-8 text-center rounded-xl bg-slate-950/40 border border-slate-800/60 text-slate-500 text-xs">
                No threats detected yet. Run detection on an investigation to populate alerts.
              </div>
            )}
          </div>
        </div>
      </div>

      {selectedThreat && (
        <ThreatDetailDrawer
          threat={selectedThreat}
          onClose={() => setSelectedThreat(null)}
          onStatusUpdated={(updated) => {
            setSelectedThreat(updated);
            loadData();
          }}
        />
      )}
    </div>
  );
}
