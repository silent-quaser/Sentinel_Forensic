export type InvestigationStatus = 'OPEN' | 'IN_PROGRESS' | 'CLOSED' | 'ARCHIVED';
export type ThreatSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type ThreatStatus = 'DETECTED' | 'REVIEWED' | 'CONFIRMED' | 'FALSE_POSITIVE' | 'RESOLVED';

export interface Investigation {
  id: number;
  investigationId: string;
  name: string;
  investigatorName: string;
  description: string;
  status: InvestigationStatus;
  createdAt: string;
  updatedAt: string;
  eventCount?: number;
  threatCount?: number;
}

export interface CreateInvestigationPayload {
  name: string;
  investigatorName: string;
  description: string;
}

export interface LogEntry {
  id: number;
  investigationId: number;
  timestamp: string;
  eventType: string;
  username: string;
  source: string;
  severity: string;
  description: string;
  rawMessage: string;
  metadata?: Record<string, any>;
}

export interface TimelineEvent {
  id: number;
  investigationId: number;
  timestamp: string;
  eventType: string;
  username: string;
  source: string;
  severity: string;
  description: string;
  rawMessage: string;
  suspicious: boolean;
  associatedThreatTitles: string[];
  associatedThreatIds: number[];
}

export interface Threat {
  id: number;
  investigationId: number;
  ruleCode: string;
  title: string;
  description: string;
  explanation: string;
  severity: ThreatSeverity;
  score: number;
  status: ThreatStatus;
  firstObserved: string;
  lastObserved: string;
  detectedAt: string;
  affectedUser: string;
  affectedIp: string;
  escalationReason?: string;
  scoreBreakdown?: string;
  evidenceEvents: LogEntry[];
}

export interface ThreatRule {
  id: number;
  ruleCode: string;
  name: string;
  description: string;
  severity: ThreatSeverity;
  threshold: number;
  timeWindowSeconds: number;
  timeWindowMinutes: number;
  enabled: boolean;
  createdAt: string;
}

export interface InvestigationNote {
  id: number;
  investigationId: number;
  author: string;
  content: string;
  createdAt: string;
}

export interface DashboardSummary {
  activeInvestigations: number;
  totalEvents: number;
  detectedThreats: number;
  highCriticalThreats: number;
  distinctUsers: number;
  threatSeverityDistribution: Record<string, number>;
  eventTypeDistribution: Record<string, number>;
  recentInvestigations: Investigation[];
  recentThreats: Threat[];
  activitySeries: {
    timestamp: string;
    eventCount: number;
    threatCount: number;
  }[];
}

export interface SystemHealth {
  status: string;
  database: string;
  threatEngine: string;
  logParser: string;
  version: string;
}

export interface DetectionResult {
  investigationId: number;
  newThreatsDetected: number;
  existingThreatsSkipped: number;
  totalThreats: number;
  threats: Threat[];
}

export interface LogUploadResult {
  investigationId: number;
  parsedCount: number;
  skippedLines: number;
  malformedLines: number;
  totalEvents: number;
  message: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}
