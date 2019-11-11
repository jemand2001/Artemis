import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';

import { SERVER_API_URL } from 'app/app.constants';
import { ProfileInfo } from 'app/layouts';
import { BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import * as _ from 'lodash';

@Injectable({ providedIn: 'root' })
export class ProfileService {
    private infoUrl = SERVER_API_URL + 'management/info';
    private profileInfo: BehaviorSubject<ProfileInfo | null>;

    constructor(private http: HttpClient) {}

    getProfileInfo(): BehaviorSubject<ProfileInfo | null> {
        if (!this.profileInfo) {
            this.profileInfo = new BehaviorSubject(null);
            this.http
                .get<ProfileInfo>(this.infoUrl, { observe: 'response' })
                .pipe(
                    map((res: HttpResponse<ProfileInfo>) => {
                        const data = res.body!;
                        const profileInfo = new ProfileInfo();
                        profileInfo.activeProfiles = data.activeProfiles;
                        const displayRibbonOnProfiles = data['display-ribbon-on-profiles'].split(',');

                        /** map guided tour configuration */
                        const guidedTourMapping = data['guided-tour'];
                        const tourArray = Object.keys(guidedTourMapping.tours).map(i => guidedTourMapping.tours[i]);
                        guidedTourMapping.tours = _.reduce(tourArray, _.extend);

                        if (profileInfo.activeProfiles) {
                            const ribbonProfiles = displayRibbonOnProfiles.filter((profile: string) => profileInfo.activeProfiles.includes(profile));
                            if (ribbonProfiles.length !== 0) {
                                profileInfo.ribbonEnv = ribbonProfiles[0];
                            }
                            profileInfo.inProduction = profileInfo.activeProfiles.includes('prod');
                        }
                        profileInfo.sentry = data.sentry;
                        profileInfo.guidedTourMapping = guidedTourMapping;
                        return profileInfo;
                    }),
                )
                .subscribe((profileInfo: ProfileInfo) => {
                    this.profileInfo.next(profileInfo);
                });
        }

        return this.profileInfo;
    }
}
