import { Orientation, OrientationConfiguration, UserInteractionEvent } from 'app/guided-tour/guided-tour.constants';
import { GuidedTourAssessmentTask, GuidedTourModelingTask } from 'app/guided-tour/guided-tour-task.model';

export abstract class TourStep {
    /** Selector for element that will be highlighted */
    highlightSelector?: string;
    /** Selector for the node that should listen to DOM changes during user interactions to define if the next step is ready
     *  Is especially important for UserInteractionEvent.CLICK steps since the next step after the click interaction will be triggered automatically */
    clickEventListenerSelector?: string;
    /** The position where the tour step will appear next to the selected element */
    orientation?: Orientation | OrientationConfiguration[] | undefined;
    /** Action that happens when the step is opened */
    action?: () => void;
    /** Action that is performed when the step is closed */
    closeAction?: () => void;
    /** Disables this step for the tour so that it won't be shown */
    disableStep?: boolean;
    /** Adds some padding for things like sticky headers when scrolling to an element */
    scrollAdjustment?: number;
    /** Adds padding around tour highlighting in pixels, this overwrites the default for this step. Is not dependent on useHighlightPadding being true */
    highlightPadding?: number;
    /** Permission to view step, if no permission is set then the tour step is visible to ROLE_USER
     * Possible inputs: 'ROLE_ADMIN', 'ROLE_INSTRUCTOR', 'ROLE_TA' */
    permission?: string[];
    /** Skips this step if the selector is not found, else the setStepAlreadyFinishedHint will be called by the guided tour service */
    skipStepIfNoSelector?: boolean;
    pageUrl?: string;
}

export class TextTourStep extends TourStep {
    /** Translation key for the title **/
    headlineTranslateKey: string;
    /** Translation key for the title **/
    subHeadlineTranslateKey?: string;
    /** Translation key for the content **/
    contentTranslateKey: string;
    /** Translation key for the hint content **/
    hintTranslateKey?: string;
    /** Translation key for the already executed hint content **/
    alreadyExecutedTranslateKey?: string;

    constructor(tourStep: TextTourStep) {
        super();
        Object.assign(this, tourStep);
    }
}

export class ImageTourStep extends TextTourStep {
    /** Image url **/
    imageUrl: string;

    constructor(tourStep: ImageTourStep) {
        super(tourStep);
        Object.assign(this, tourStep);
    }
}

export class VideoTourStep extends TextTourStep {
    /** Embed video url **/
    videoUrl: string;

    constructor(tourStep: VideoTourStep) {
        super(tourStep);
        Object.assign(this, tourStep);
    }
}

export class UserInterActionTourStep extends TextTourStep {
    /** If this is set, then the user can interact with the elements that are within the rectangle that highlights the selected element */
    userInteractionEvent: UserInteractionEvent;
    /** Enables the automatic display of the next step after a user interaction */
    triggerNextStep?: boolean;
    /** Check for next selector*/
    checkForNextStepSelector?: boolean;

    constructor(tourStep: UserInterActionTourStep) {
        super(tourStep);
        Object.assign(this, tourStep);
    }
}

export class ModelingTaskTourStep extends UserInterActionTourStep {
    /** Modeling task that has to be completed during this step */
    modelingTask: GuidedTourModelingTask;

    constructor(tourStep: ModelingTaskTourStep) {
        super(tourStep);
        Object.assign(this, tourStep);
    }
}

export class AssessmentTaskTourStep extends UserInterActionTourStep {
    /** Assessment task that has to be completed during this step */
    assessmentTask: GuidedTourAssessmentTask;

    constructor(tourStep: AssessmentTaskTourStep) {
        super(tourStep);
        Object.assign(this, tourStep);
    }
}
