import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { QuizExerciseService } from 'app/exercises/quiz/manage/quiz-exercise.service';
import { QuizQuestion, QuizQuestionType } from 'app/entities/quiz/quiz-question.model';
import { QuizExercise } from 'app/entities/quiz/quiz-exercise.model';

@Injectable({ providedIn: 'root' })
export class QuizStatisticUtil {
    constructor(private router: Router, private quizExerciseService: QuizExerciseService) {}

    /**
     * go to the Template with the previous QuizStatistic
     * if first QuizQuestionStatistic -> go to the quiz-statistic
     *
     * @param quizExercise: the quizExercise with all statistics
     * @param question: the question of the current statistic
     */
    previousStatistic(quizExercise: QuizExercise, question: QuizQuestion) {
        // find position in quiz
        const index = quizExercise.quizQuestions.findIndex(function (quiz) {
            return quiz.id === question.id;
        });
        // go to quiz-statistic if the position = 0
        if (index === 0) {
            this.router.navigateByUrl('/course-management/' + quizExercise.course?.id + '/quiz-exercises/' + quizExercise.id + '/quiz-point-statistic');
        } else {
            // go to previous Question-statistic
            this.navigateToStatisticOf(quizExercise, quizExercise.quizQuestions[index - 1]);
        }
    }

    /**
     * go to the Template with the next QuizStatistic
     * if last QuizQuestionStatistic -> go to the Quiz-point-statistic
     *
     * @param quizExercise: the quizExercise with all statistics
     * @param question: the question of the current statistic
     */
    nextStatistic(quizExercise: QuizExercise, question: QuizQuestion) {
        // find position in quiz
        const index = quizExercise.quizQuestions.findIndex(function (quiz) {
            return quiz.id === question.id;
        });
        // go to quiz-statistic if the position = last position
        if (index === quizExercise.quizQuestions.length - 1) {
            this.router.navigateByUrl('/course-management/' + quizExercise.course?.id + '/quiz-exercises/' + quizExercise.id + '/quiz-point-statistic');
        } else {
            // go to next Question-statistic
            this.navigateToStatisticOf(quizExercise, quizExercise.quizQuestions[index + 1]);
        }
    }

    navigateToStatisticOf(quizExercise: QuizExercise, question: QuizQuestion) {
        if (question.type === QuizQuestionType.MULTIPLE_CHOICE) {
            this.router.navigateByUrl(`/course-management/${quizExercise.course?.id}/quiz-exercises/${quizExercise.id}/mc-question-statistic/${question.id}`);
        } else if (question.type === QuizQuestionType.DRAG_AND_DROP) {
            this.router.navigateByUrl(`/course-management/${quizExercise.course?.id}/quiz-exercises/${quizExercise.id}/dnd-question-statistic/${question.id}`);
        } else if (question.type === QuizQuestionType.SHORT_ANSWER) {
            this.router.navigateByUrl(`/course-management/${quizExercise.course?.id}/quiz-exercises/${quizExercise.id}/sa-question-statistic/${question.id}`);
        }
    }
}
