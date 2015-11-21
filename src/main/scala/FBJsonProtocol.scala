import spray.routing._
import spray.json._
import spray.httpx._
import spray.http._

case class Album(id: String, description: String)

object FBJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {  

    implicit val albumFormat = jsonFormat2(Album)

  
/*  case object QuizCreated
  
  case object QuizAlreadyExists
  
  case object QuizDeleted
  
  
  case class Question(id: String, question: String)
  
  case object QuestionNotFound
  
  
  case class Answer(answer: String)
  
  case object CorrectAnswer
  
  case object WrongAnswer
  
  /* json (un)marshalling */
  
  object Quiz extends DefaultJsonProtocol {
    implicit val format = jsonFormat3(Quiz.apply)
  }

  object Question extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(Question.apply)
  }

  object Answer extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(Answer.apply)
  }
  
  /* implicit conversions */

  implicit def toQuestion(quiz: Quiz): Question = Question(id = quiz.id, question = quiz.question)

  implicit def toAnswer(quiz: Quiz): Answer = Answer(answer = quiz.correctAnswer)*/
}