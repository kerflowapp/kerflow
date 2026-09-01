export type MessageDirection = 'OUTBOUND' | 'INBOUND';

/** Outbound only: DRAFT until the message is actually sent. Null for inbound. */
export type MessageStatus = 'DRAFT' | 'SENT';

/** Support of the exchange. Null on messages recorded before channels existed → read as EMAIL. */
export type MessageChannel = 'EMAIL' | 'PHONE' | 'SMS' | 'LINKEDIN' | 'MEETING' | 'OTHER';

export interface ProspectMessageDto {
	id: string;
	direction: MessageDirection;
	channel?: MessageChannel | null;
	status?: MessageStatus | null;
	subject?: string | null;
	body: string;
	/** "mcp-agent" when written by an MCP client; null when typed by the user */
	generatedBy?: string | null;
	sentAt?: string | null;
	receivedAt?: string | null;
	creationDate: string;
}

export interface CreateProspectMessageDto {
	/** Omitted → INBOUND */
	direction?: MessageDirection;
	/** Omitted → EMAIL */
	channel?: MessageChannel;
	/** Outbound only, ignored on inbound. Omitted → DRAFT */
	status?: MessageStatus;
	subject?: string;
	body: string;
	/** ISO instant of the exchange. Omitted → now */
	occurredAt?: string;
}
