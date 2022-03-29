
import enigma.core.Enigma;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.Random;

public class Game {

   private static char[] elements = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', '*', '/'};

   public static enigma.console.Console cn = Enigma.getConsole("POST-FIXER", 80, 30, 25, 0);

   private static Random rnd = new Random();

   // ------ Standard variables for mouse and keyboard ------

   public int keypr;   // key pressed?
   public int rkey;    // key   (for press/release)

   public KeyListener klis = new KeyListener() {

      public void keyTyped(KeyEvent e) {}

      public void keyPressed(KeyEvent e) {

         if(keypr==0) {

            keypr=1;
            rkey=e.getKeyCode();

         }

      }

      public void keyReleased(KeyEvent e) {}

   };

   // ----------------------------------------------------

   private char[][] BOARD = new char[10][10];
   private int elementCount = 0;

   private CircularQueue queue = new CircularQueue(8);

   private int general_score = 0;
   private int time = 60;
   private int px = 5, py = 5;
   private int expressionOffset = 0;

   private Stack stack = new Stack(10000);
   private Stack STACK11 = new Stack(1000);// to evaluate expression

   public Game() throws Exception {   // --- Contructor



      int current_index;//if there is only dots on the road , to hold cursor on the same index
      boolean flag;
      int score = 0;

      initUI();
      initQueue();
      initBoard();
      fillBoard();

      // ----------------------------------------------------

      String temporary = "";

      char board_char = ' '; //current char on the board
      int printing_once_W = 0; //walk mode

      cn.getTextWindow().setCursorPosition(px, py);

      while (true) {

         if (printing_once_W == 0) {

            cn.getTextWindow().setCursorPosition(45, 4);
            cn.getTextWindow().output(" WALK    ");
            cn.getTextWindow().setCursorPosition(px, py);
            printing_once_W++;

         }

         printingBoard();
         // if keyboard button pressed
         if (keypr == 1) {

        	 cleaningScreen();//when player moves clean second expression and box
        	 
            if (rkey == KeyEvent.VK_A) px--;
            if (rkey == KeyEvent.VK_D) px++;
            if (rkey == KeyEvent.VK_W) py--;
            if (rkey == KeyEvent.VK_S) py++;

            //frame check
            if (px < 2) {
               px++;
            } else if (px > 11) {
               px--;
            } else if (py < 2) {
               py++;
            } else if (py > 11) {
               py--;
            }

            cn.getTextWindow().setCursorPosition(px, py);

            if (printing_once_W >= 2) {
               Thread.sleep(500);
               cn.getTextWindow().setCursorPosition(45, 4);
               cn.getTextWindow().output("   FREE    ");
               cn.getTextWindow().setCursorPosition(px, py);
            }

            if ((rkey == KeyEvent.VK_T)) {

               cn.getTextWindow().setCursorPosition(45, 4);
               cn.getTextWindow().output("    TAKE    ");
               cn.getTextWindow().setCursorPosition(px, py);

               while (true) {

                  Thread.sleep(1000);
                  time--;

                  if (time > -1) {

                     if (time < 60 && time > 9) {

                        char x = String.valueOf(time).charAt(0);
                        char y = String.valueOf(time).charAt(1);
                        cn.getTextWindow().output(46, 2, x);
                        cn.getTextWindow().output(47, 2, y);

                     } else {

                        char x = String.valueOf(time).charAt(0);
                        cn.getTextWindow().output(46, 2, x);
                        cn.getTextWindow().output(47, 2, ' ');

                     }

                  } 
                  else {break;}

                  if ((rkey == KeyEvent.VK_D && keypr != 0)) {

                     current_index = px;
                     flag = false;

                     cn.getTextWindow().setCursorPosition(px, py);

                     while ((String.valueOf(BOARD[px - 2][py - 2]).equals("."))) {

                        if (px != 11)
                           px++;

                        if (px == 11 && (String.valueOf(BOARD[px - 2][py - 2]).equals(".")))//only dots on the road
                           break;

                     }

                     while (!(String.valueOf(BOARD[px - 2][py - 2]).equals("."))) {
                        flag = true;
                        board_char = BOARD[px - 2][py - 2];
                        if (!String.valueOf(board_char).equals(".") && (String.valueOf(board_char).equals("+") || String.valueOf(board_char).equals("-") ||
                                String.valueOf(board_char).equals("/") || String.valueOf(board_char).equals("*"))) {

                           temporary = String.valueOf(board_char);
                           BOARD[px - 2][py - 2] = '.';
                           elementCount--;// taken elements numbers from the board
                           break;

                        } else if (!String.valueOf(board_char).equals(".") && !String.valueOf(board_char).equals("+") && !String.valueOf(board_char).equals("-")//sayiyi alma
                                && !String.valueOf(board_char).equals("/") && !String.valueOf(board_char).equals("*")) {

                           temporary = temporary + String.valueOf(board_char);
                           BOARD[px - 2][py - 2] = '.';
                           elementCount--;//taken elements numbers from the board
                           if (px != 11) {
                              if (px != 11 && ((String.valueOf(BOARD[px - 1][py - 2]).equals("+") || String.valueOf(BOARD[px - 1][py - 2]).equals("-") ||//sayidan sonra hemen iþleç gelirse
                                      String.valueOf(BOARD[px - 1][py - 2]).endsWith("/") || String.valueOf(BOARD[px - 1][py - 2]).equals("*")))) {
                                 break;
                              }
                              if (px != 11 && (String.valueOf(BOARD[px - 1][py - 2]).equalsIgnoreCase(".")))
                              {
                                 break;
                              }
                           }

                           if (px == 11 && (!String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("+")) && !String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("-") &&//sinirdaysa
                                   !String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("*") && !String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("/")) {
                              break;
                           }

                        }
                        px++;
                        if (px == 12) {
                           px = 11;
                        }
                     }
                     if (flag == true) {
                        px++;
                        if (!temporary.equals("")) {
                           stack.push(temporary);
                           temporary = "";
                           String string = (String) stack.peek();
                           int expression_length = string.length();
                           cn.getTextWindow().setCursorPosition(28 + expressionOffset, 13); // expression
                           cn.getTextWindow().output(string);
                           cn.getTextWindow().setCursorPosition(28 + expressionOffset, 15); // expression
                           cn.getTextWindow().output(string);
                           expressionOffset += expression_length + 1;
                        }
                        printingBoard();
                        cn.getTextWindow().setCursorPosition(px, py);
                        px--;
                        cn.getTextWindow().setCursorPosition(px, py);
                     } else //if there is only dots on the road , to hold cursor on the same index
                     {
                        px = current_index;
                        cn.getTextWindow().setCursorPosition(px, py);
                     }

                     keypr = 0;

                  }

                  if ((rkey == KeyEvent.VK_A && keypr != 0)) {
                     flag = false;
                     current_index = px;
                     cn.getTextWindow().setCursorPosition(px, py);
                     while ((String.valueOf(BOARD[px - 2][py - 2]).equals(".")))//ifadeyi bulana kadar indeksi azaltir
                     {
                        if (px != 2) {
                           px--;
                        }
                        if (px == 2 && (String.valueOf(BOARD[px - 2][py - 2]).equals("."))) {
                           break;
                        }
                     }
                     while (!(String.valueOf(BOARD[px - 2][py - 2]).equals("."))) {
                        flag = true;
                        board_char = BOARD[px - 2][py - 2];
                        if (!String.valueOf(board_char).equals(".") && (String.valueOf(board_char).equals("+") || String.valueOf(board_char).equals("-") || String.valueOf(board_char).equals("/") || String.valueOf(board_char).equals("*"))) {
                           temporary = String.valueOf(board_char);
                           BOARD[px - 2][py - 2] = '.';
                           elementCount--;// taken elements numbers from the board
                           break;
                        } else if (!String.valueOf(board_char).equals(".") && !String.valueOf(board_char).equals("+") || String.valueOf(board_char).equals("-") &&
                                !String.valueOf(board_char).equals("/") && !String.valueOf(board_char).equals("*")) {
                           temporary = temporary + String.valueOf(board_char);
                           BOARD[px - 2][py - 2] = '.';
                           elementCount--;

                           if (px != 2) {
                              if ((px != 2 && ((String.valueOf(BOARD[px - 3][py - 2]).equalsIgnoreCase(".")) || String.valueOf(BOARD[px - 3][py - 2]).equalsIgnoreCase("+")) || String.valueOf(BOARD[px - 3][py - 2]).equalsIgnoreCase("-")
                                      || String.valueOf(BOARD[px - 3][py - 2]).equalsIgnoreCase("*") || String.valueOf(BOARD[px - 3][py - 2]).equalsIgnoreCase("/"))) // bir öncekinde sayi bir sonraki indekste operator varsa
                              {
                                 break;
                              }
                              if (px != 2 && (String.valueOf(BOARD[px - 3][py - 2]).equalsIgnoreCase("."))) 
                              {

                                 break;
                              }
                           }
                           if (px == 2 && (!String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("+")) && !String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("-") &&
                                   !String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("*") && !String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("/"))  //sayinin son hanesi sinirdaysa
                           {
                              break;
                           }
                        }
                        px--;
                     }
                     if (flag == true) {
                        px--;
                        if (!temporary.equals("")) {
                           stack.push(temporary);
                           temporary = "";
                           String string = (String) stack.peek();
                           int expression_length = string.length();
                           cn.getTextWindow().setCursorPosition(28 + expressionOffset, 13); // expression
                           cn.getTextWindow().output(string);
                           cn.getTextWindow().setCursorPosition(28 + expressionOffset, 15); // expression
                           cn.getTextWindow().output(string);
                           expressionOffset += expression_length + 1;
                        }
                        printingBoard();
                        cn.getTextWindow().setCursorPosition(px, py);
                        px++;
                        cn.getTextWindow().setCursorPosition(px, py);
                     } else //if there is only dots on the road , to hold cursor on the same index
                     {
                        px = current_index;
                        cn.getTextWindow().setCursorPosition(px, py);
                     }

                     keypr = 0;
                  }

                  if ((rkey == KeyEvent.VK_W && keypr != 0)) {
                     flag = false;
                     current_index = py;
                     while ((String.valueOf(BOARD[px - 2][py - 2]).equals(".")))// bir ifadeyle karþilaþana kadar indeksi azaltir
                     {
                        if (py != 2) {
                           py--;
                        }
                        if (py == 2 && (String.valueOf(BOARD[px - 2][py - 2]).equals("."))) //border
                        {
                           break;
                        }
                     }
                     cn.getTextWindow().setCursorPosition(px, py);
                     while (!(String.valueOf(BOARD[px - 2][py - 2]).equals("."))) {
                        flag = true;
                        board_char = BOARD[px - 2][py - 2];
                        if (!String.valueOf(board_char).equals(".") && (String.valueOf(board_char).equals("+") || String.valueOf(board_char).equals("-") || String.valueOf(board_char).equals("/") || String.valueOf(board_char).equals("*"))) {
                           temporary = String.valueOf(board_char);
                           BOARD[px - 2][py - 2] = '.';
                           elementCount--;// taken elements numbers from the board
                           break;
                        } else if (!String.valueOf(board_char).equals(".") && !String.valueOf(board_char).equals("+") || String.valueOf(board_char).equals("-") &&
                                !String.valueOf(board_char).equals("/") && !String.valueOf(board_char).equals("*")) {
                           temporary = temporary + String.valueOf(board_char);
                           BOARD[px - 2][py - 2] = '.';
                           elementCount--;
                           if (py != 2) {
                              if ((py != 2 && ((String.valueOf(BOARD[px - 2][py - 3]).equalsIgnoreCase(".")) || String.valueOf(BOARD[px - 2][py - 3]).equalsIgnoreCase("+")) || String.valueOf(BOARD[px - 2][py - 3]).equalsIgnoreCase("-")
                                      || String.valueOf(BOARD[px - 2][py - 3]).equalsIgnoreCase("*") || String.valueOf(BOARD[px - 2][py - 3]).equalsIgnoreCase("/"))) 
                              {
                                 break;
                              }
                              if (py != 2 && (String.valueOf(BOARD[px - 2][py - 3]).equalsIgnoreCase("."))) 
                              {
                                 break;
                              }
                           }
                           if (py == 2 && (!String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("+")) && !String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("-") &&
                                   !String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("*") && !String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("/")) {
                              break;
                           }

                        }
                        py--;
                     }
                     if (flag == true) {
                        py--;
                        if (!temporary.equals("")) {
                           stack.push(temporary);
                           temporary = "";
                           String string = (String) stack.peek();
                           int expression_length = string.length();
                           cn.getTextWindow().setCursorPosition(28 + expressionOffset, 13); // expression
                           cn.getTextWindow().output(string);
                           cn.getTextWindow().setCursorPosition(28 + expressionOffset, 15); // expression
                           cn.getTextWindow().output(string);
                           expressionOffset += expression_length + 1;
                        }
                        printingBoard();
                        cn.getTextWindow().setCursorPosition(px, py);
                        py++;
                        cn.getTextWindow().setCursorPosition(px, py);
                     } else //if there is only dots on the road , to hold cursor on the same index
                     {
                        py = current_index;
                        cn.getTextWindow().setCursorPosition(px, py);
                     }
                     keypr = 0;
                  }

                  if ((rkey == KeyEvent.VK_S && keypr != 0)) {
                     current_index = py;
                     flag = false;
                     cn.getTextWindow().setCursorPosition(px, py);
                     while ((String.valueOf(BOARD[px - 2][py - 2]).equals("."))) {
                        if (py != 11) {
                           py++;
                        }
                        if (py == 11 && (String.valueOf(BOARD[px - 2][py - 2]).equals("."))) {
                           break;
                        }
                     }
                     while (!(String.valueOf(BOARD[px - 2][py - 2]).equals("."))) {
                        flag = true;
                        board_char = BOARD[px - 2][py - 2];
                        if (!String.valueOf(board_char).equals(".") && (String.valueOf(board_char).equals("+") || String.valueOf(board_char).equals("-") ||//operator control
                                String.valueOf(board_char).equals("/") || String.valueOf(board_char).equals("*"))) {
                           temporary = String.valueOf(board_char);
                           BOARD[px - 2][py - 2] = '.';
                           elementCount--;// BOARD'dan ALINAN ELEMAN KADAR BOARD TEKRAR DOLDURULACAk
                           break;
                        } else if (!String.valueOf(board_char).equals(".") && !String.valueOf(board_char).equals("+") && !String.valueOf(board_char).equals("-") &&//digits control
                                !String.valueOf(board_char).equals("/") && !String.valueOf(board_char).equals("*")) {
                           temporary = temporary + String.valueOf(board_char);
                           BOARD[px - 2][py - 2] = '.';
                           elementCount--;
                           if (py != 11) {
                              if (py != 11 && (String.valueOf(BOARD[px - 2][py - 1]).equalsIgnoreCase("+")) || String.valueOf(BOARD[px - 2][py - 1]).equalsIgnoreCase("-") || String.valueOf(BOARD[px - 2][py - 1]).equalsIgnoreCase("*") || String.valueOf(BOARD[px - 2][py - 1]).equalsIgnoreCase("/")) {
                                 break;
                              }
                              if (py != 11 && (String.valueOf(BOARD[px - 2][py - 1]).equalsIgnoreCase(".")))
                              {
                                 break;
                              }

                           }
                           if (py == 11 && (!String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("+")) && !String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("-") &&
                                   !String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("*") && !String.valueOf(BOARD[px - 2][py - 2]).equalsIgnoreCase("/")) {
                              break;
                           }
                        }
                        py++;
                        if (py == 13) {
                           py = 12;
                        }
                     }
                     if (flag == true) {
                        py++;
                        if (!temporary.equals("")) {
                           stack.push(temporary);
                           temporary = "";
                           String string = (String) stack.peek();
                           int expression_length = string.length();
                           cn.getTextWindow().setCursorPosition(28 + expressionOffset, 13); // expression
                           cn.getTextWindow().output(string);
                           cn.getTextWindow().setCursorPosition(28 + expressionOffset, 15);
                           cn.getTextWindow().output(string);
                           expressionOffset += expression_length + 1;

                        }
                        printingBoard();
                        cn.getTextWindow().setCursorPosition(px, py);
                        py--;
                        cn.getTextWindow().setCursorPosition(px, py);
                     } else //if there is only dots on the road , to hold cursor on the same index
                     {
                        py = current_index;
                        cn.getTextWindow().setCursorPosition(px, py);
                     }
                     keypr = 0;
                  }


                  if (rkey == KeyEvent.VK_F) {
                     break;
                  }

                  if (time == 0) {
                     cn.getTextWindow().setCursorPosition(25, 17);
                     System.out.println("RUN OUT OF TIME");
                     break;
                  }

                  keypr = 0;

               }

            }

            if (rkey == KeyEvent.VK_F) 
            {

               cn.getTextWindow().setCursorPosition(45, 4);
               cn.getTextWindow().output(" EVALUATION");
               cn.getTextWindow().setCursorPosition(px, py);

               //filling board again
               fillBoard();
               printingBoard();

               int size = stack.size();

               try {

                  score = evaluator();

               } catch (Exception e) {

                  // Error messages
                  if (e.getMessage() == "FaultyExpression" || e.getMessage() == "DivisionByZero")
                     score = -20;

               }

               general_score += score;

               int length_score = String.valueOf(general_score).length();

               for (int i = 0; i < length_score; i++) // printing score on the board
                  cn.getTextWindow().output(48 + i, 3, Integer.toString(general_score).charAt(i)); // digits

               for (int i = 0; i < size + 8; i++) //deleting first expression 
                  cn.getTextWindow().output(28 + i, 13, ' ');

               expressionOffset = 0;

            }

            keypr = 0;

         }

         Thread.sleep(500);

         if (time == 0)
            break;

      }

   }

   public void initUI() {

      cn.getTextWindow().addKeyListener(klis);

      cn.getTextWindow().setCursorType(1);

      cn.getTextWindow().setCursorPosition(15, 2);
      cn.getTextWindow().output("input");
      cn.getTextWindow().setCursorPosition(40, 2);
      cn.getTextWindow().output("Time:");
      cn.getTextWindow().setCursorPosition(40, 3);
      cn.getTextWindow().output("Score:");
      cn.getTextWindow().setCursorPosition(40, 4);
      cn.getTextWindow().output("Mode:");
      cn.getTextWindow().setCursorPosition(15, 13);
      cn.getTextWindow().output("Expression:");
      cn.getTextWindow().setCursorPosition(15, 15);
      cn.getTextWindow().output("Expression:");

      int frame = 1;

      for (int i = 1; i < 13; i++) // BOARD FRAME
      {

         cn.getTextWindow().output(1, i, '#');
         cn.getTextWindow().output(12, i, '#');
         cn.getTextWindow().output(i, 1, '#');
         cn.getTextWindow().output(i, 12, '#');
         cn.getTextWindow().output(1, i, '#');

         if (i >= 1 && i < 11) {

            cn.getTextWindow().output(0, i + 1, Integer.toString(frame).charAt(0));
            cn.getTextWindow().output(i + 1, 0, Integer.toString(frame).charAt(0));

            frame++;

            if (frame > 9)
               frame = 0;


         }

         if (i > 5) {

            //10 br
            cn.getTextWindow().output(50, 2 + i, '|');
            cn.getTextWindow().output(57, 2 + i, '|');
            cn.getTextWindow().output(44 + i, 14, '-');

         }

      }

   }

   public void initQueue() {

      for (int i = 0; i < 8; i++) {

         char x = elements[rnd.nextInt(13)];
         queue.enqueue(x);

      }

   }

   public void initBoard() {

      for (int i = 0; i < BOARD.length; i++) // FILL BOARD[][] ARRAY
      {
         for (int j = 0; j < BOARD[i].length; j++)
            BOARD[i][j] = '.';
      }

   }

   public void fillBoard() {

      while (elementCount != 40)
      {

         int random_x = rnd.nextInt(10);
         int random_y = rnd.nextInt(10);

         if (BOARD[random_x][random_y] == '.') {

            BOARD[random_x][random_y] = (char) queue.peek();
            queue.dequeue();

            char x = elements[rnd.nextInt(13)];

            queue.enqueue(x);
            elementCount++;

         }

         //printing queue on the board
         for (int i = 0; i < queue.size(); i++) {

            cn.getTextWindow().output(15 + i, 3, '<');
            cn.getTextWindow().output(15 + i, 4, (char) queue.peek());
            queue.enqueue(queue.dequeue());
            cn.getTextWindow().output(15 + i, 5, '<');

         }

      }

   }

   public void printingBoard() {

      for (int i = 0; i < BOARD.length; i++) {
         for (int j = 0; j < BOARD[i].length; j++)
            cn.getTextWindow().output(i + 2, j + 2, BOARD[i][j]);
      }

   }
   public void cleaningScreen() 
   {
	   for(int i=0;i<14;i++) 
 	  {
 		 if(i<6) 
 		 {
 			 cn.getTextWindow().output(51+i, 13, ' ');
 			 cn.getTextWindow().output(51+i, 12, ' ');

 		 }
 		 cn.getTextWindow().output(28+i, 15, ' ');//10br

 	  }
   }

   public int calculatingScore(Stack stackk){

      int High_Score = 0;
      Stack staack = new Stack(100);
      Stack stack = new Stack(100);

      int counter_oper = 0;
      int digit = 0;
      int score;
      int size = stackk.size();

      for (int i = 0; i < size; i++) {
         if (((String) stackk.peek()).equals("+") || ((String) stackk.peek()).equals("-") || ((String) stackk.peek()).equals("*") || ((String) stackk.peek()).equals("/")) {
            counter_oper++;
         }//if operators more than numbers -20 point
         staack.push(stackk.pop());
      }

      for (int i = 0; i < size; i++) {
         stack.push(staack.pop());
      }

      size = stack.size();
      Object temp;

      int length = ((String) stack.peek()).length();

      if (length > 1) //score should start with this calculation
      {
         digit = length * 2;
         score = digit;
      } else {
         score = 1;
      }
      for (int i = 0; i < size - 1; i++)
      {
         temp = stack.peek();
         staack.push(stack.pop());
         if ((temp.equals("+") || temp.equals("-") || temp.equals("*") || temp.equals("/")) &&
                 (stack.peek().equals("+") || stack.peek().equals("-") || stack.peek().equals("/") || stack.peek().equals("*"))) {
            score++;
         } else if ((temp.equals("+") || temp.equals("-") || temp.equals("*") || temp.equals("/")) &&
                 (!stack.peek().equals("+") || !stack.peek().equals("-") || !stack.peek().equals("/") || !stack.peek().equals("*"))) {
            if (Integer.parseInt((String) stack.peek()) > 9) {
               length = ((String) stack.peek()).length();
               digit = length * 2;
               score += digit;
            } else {
               score += 2;
            }
         } else if ((!temp.equals("+") && !temp.equals("-") && !temp.equals("*") && !temp.equals("/")) && // first one operand second one number
                 (stack.peek().equals("+") || stack.peek().equals("-") || stack.peek().equals("/") || stack.peek().equals("*"))) {
            if (Integer.parseInt((String) temp) > 9) {
               length = ((String) temp).length();
               digit = length * 2;
               score += digit;
            } else {
               score += 2;
            }
         } else if ((!temp.equals("+") && !temp.equals("-") && !temp.equals("*") && !temp.equals("/")) && // both numbers
                 (!stack.peek().equals("+") || !stack.peek().equals("-") || !stack.peek().equals("/") || !stack.peek().equals("*"))) {
            length = ((String) temp).length();
            int length_2 = ((String) stack.peek()).length();

            if ((length == length_2 && length < 2) || (length > 1 && length_2 == 1)) //like 4 9 or 45 2 
            {
               score++;
            } else //4 44
            {
               digit = length_2 * 2;
               score += digit;
            }

         }
         if (i == size - 2) {
            staack.push(stack.pop());
         }
      }
      if (!(counter_oper == 0) && (size - counter_oper - counter_oper == 1)) {
         High_Score = score * score;
      } else {
         High_Score = -20;
      }
      for (int j = 0; j < size; j++) {
         stackk.push(staack.pop());
      }

      //The taken stack was filled but reverse
      return High_Score;
   }

   public int evaluator() throws Exception {

      int size = stack.size();

      int score = calculatingScore(stack);

      for (int i = 0; i < size; i++)
         STACK11.push(stack.pop());

      Stack calcStack = new Stack(size);

      // While inputStack is not empty

      while (!STACK11.peek().equals(0)) {

         keypr = 0;

         String element = (String) STACK11.pop();

         boolean isOperator = (element.equals("+") || element.equals("-") || element.equals("/") || element.equals("*"));

         if (isOperator) {

            // Operators need 2 operands. If there aren't, throws "FaultyExpression" exception
            if (calcStack.size() < 2)
               throw new Exception("FaultyExpression");

            int operand1 = (int) calcStack.pop();
            int operand2 = (int) calcStack.pop();
            int result = 0;

            if (element == "/" && operand1 == 0)
               throw new Exception("DivisionByZero");

            switch (element) {
               case "+":
                  calcStack.push(operand2 + operand1);
                  break;
               case "-":
                  calcStack.push(operand2 - operand1);
                  break;
               case "/":
                  calcStack.push(operand2 / operand1);
                  break;
               case "*":
                  calcStack.push(operand2 * operand1);
                  break;
            }

         } else {

            calcStack.push(Integer.parseInt(element));

         }

         // Wait until user presses space
         while(true) {

            if (keypr == 1 && rkey == KeyEvent.VK_SPACE)
               break;

            Thread.sleep(500);

         }



         // Print calcStack vertically

         int printableCount = calcStack.size();

         Stack printStack = new Stack(calcStack.size());

         for (int i = 0; i < printableCount; i++)
            printStack.push(calcStack.pop());

         for (int i = 0; i < printableCount; i++) {

            Object printableElement = printStack.pop();

            String str = Integer.toString((int) printableElement);

            for (int j = 0; j < 5; j++) {

               cn.getTextWindow().output(56 - j, 13 - i, ' ');

               if (j < str.length())
                  cn.getTextWindow().output(56 - j, 13 - i, str.charAt(str.length() - 1 - j));

            }

            calcStack.push(printableElement);
            
         }

      }

      keypr = 0;

      // There has to be only one element left after all operations, else throws "FaultyExpression" exception
      if (calcStack.size() == 1) 
      {
    	  for (int k = 0; k < 2; k++) // cleaning
          {
             cn.getTextWindow().output(54+k, 12, ' ');
             cn.getTextWindow().output(53+k, 12, ' ');
             cn.getTextWindow().output(51+k, 12, ' ');
             cn.getTextWindow().output(53+k, 11 + k, ' ');
             cn.getTextWindow().output(55+k, 11 + k, ' ');
             cn.getTextWindow().output(51+k, 11 + k, ' ');

          }
          return score;

      }
      else
         throw new Exception("FaultyExpression");

   }

}
