import { Moment } from 'moment';
import { User } from 'app/core/user/user.model';
import { BaseEntity } from 'app/shared/model/base-entity';
import { Result } from 'app/entities/result.model';

export enum ComplaintType {
    COMPLAINT = 'COMPLAINT',
    MORE_FEEDBACK = 'MORE_FEEDBACK',
}

export class Complaint implements BaseEntity {
    public id: number;

    public complaintText: string;
    public accepted: boolean;
    public submittedTime: Moment | null;
    public resultBeforeComplaint: string;
    public result: Result;
    public student: User;
    public complaintType: ComplaintType;

    constructor() {}
}
