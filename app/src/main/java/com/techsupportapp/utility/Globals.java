package com.techsupportapp.utility;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;

import java.util.ArrayList;

public class Globals {

    public static User currentUser;

    /**
     * Метод, вызывающий информацию о приложении.
     * @param context Контекст вызывающего класса.
     */
    public static void showAbout(Context context) {
        new MaterialDialog.Builder(context)
                .title("О программе")
                .content(String.format("Tech Support App V%s", context.getString(R.string.app_version)))
                .positiveText(android.R.string.ok)
                .show();
    }

    /**
     * Метод, инициализирующий и отображающий долговременное, всплываюее сообщение.
     * @param context Контекст приложения.
     * @param message Сообщение.
     */
    public static void showLongTimeToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static boolean isEnglishWord(String word){
        for (int i = 0; i < word.length(); i++)
            if (!isEnglishLetterOrDigit(word.charAt(i)))
                return false;
        return true;
    }

    private static boolean isEnglishLetterOrDigit(char c) {
        return 'A' <= c && c <= 'Z' || 'a' <= c && c <= 'z' || '0' <= c && c <= '9';
    }

    public static class ImageMethods {
        /**
         * Метод, создающий картинку пользователя с первой буквой его имени по центру.
         * @param name Отображаемое имя пользователя.
         * @param context Контекст вызывающего класса.
         * @return Возвращает картинку (класс Bitmap) с первой буквой имени пользователя по центру.
         */
        public static Bitmap createUserImage(String name, Context context) {
            int COVER_IMAGE_SIZE = 100;
            LetterBitmap letterBitmap = new LetterBitmap(context);
            Bitmap letterTile = letterBitmap.getLetterTile(name.substring(0), name, COVER_IMAGE_SIZE, COVER_IMAGE_SIZE);
            return (letterTile);
        }

        /**
         * Метод, задающий округлую форму картинки.
         * @param bitmap Изображение, форму которого необходимо изменить.
         * @return Возвращает закругленную картинку (класс Bitmap).
         */
        public static Bitmap getclip(Bitmap bitmap) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                    bitmap.getWidth() / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return output;
        }
    }

    public static class Downloads {

        /**
         * Метод, скачивающий из базы данных всех подтвержденных пользователей, независимо от прав.
         * @param dataSnapshot Снимок базы данных.
         * @return Всех подтвержденных пользователей.
         */
        public static ArrayList<User> getVerifiedUserList(DataSnapshot dataSnapshot) {
            ArrayList<User> resultList = new ArrayList<User>();
            resultList.addAll(getSpecificVerifiedUserList(dataSnapshot, DatabaseVariables.Users.DATABASE_VERIFIED_ADMIN_TABLE));
            resultList.addAll(getSpecificVerifiedUserList(dataSnapshot, DatabaseVariables.Users.DATABASE_VERIFIED_CHIEF_TABLE));
            resultList.addAll(getSpecificVerifiedUserList(dataSnapshot, DatabaseVariables.Users.DATABASE_VERIFIED_SIMPLE_USER_TABLE));
            resultList.addAll(getSpecificVerifiedUserList(dataSnapshot, DatabaseVariables.Users.DATABASE_VERIFIED_WORKER_TABLE));
            return resultList;
        }

        /**
         * Метод, скачивающий из базы данных подтвержденных пользователей с определенными правами.
         * @param dataSnapshot Снимок базы данных.
         * @param databaseTablePath Путь в базе данных к необходимой категории подтвержденных пользователей.
         * @return Всех подтвержденных пользователей с определенными правами.
         */
        public static ArrayList<User> getSpecificVerifiedUserList(DataSnapshot dataSnapshot, String databaseTablePath) {
            ArrayList<User> resultList = new ArrayList<User>();
            for (DataSnapshot userRecord : dataSnapshot.child(databaseTablePath).getChildren())
                resultList.add(userRecord.getValue(User.class));
            return resultList;
        }

        /**
         * Метод, скачивающий из базы данных логины неподтвержденных пользователей.
         * @param dataSnapshot Снимок базы данных.
         * @return Логины неподтвержденных пользователей.
         */
        public static ArrayList<String> getUnverifiedLogins(DataSnapshot dataSnapshot) {
            ArrayList<String> resultList = new ArrayList<String>();
            for (DataSnapshot userRecord : dataSnapshot.child(DatabaseVariables.Users.DATABASE_UNVERIFIED_USER_TABLE).getChildren())
                resultList.add(userRecord.getValue(User.class).getLogin());
            return resultList;
        }

        /**
         * Метод, скачивающий из базы данных логины либо подтвержденных, либо неподтвержденных пользователей.
         * @param dataSnapshot Снимок базы данных.
         * @return Логины выбранной категории пользователей.
         */
        public static ArrayList<String> getVerifiedLogins(DataSnapshot dataSnapshot) {
            ArrayList<String> resultList = new ArrayList<String>();
            ArrayList<User> userList = getVerifiedUserList(dataSnapshot);
            for (User user : userList)
                resultList.add(user.getLogin());
            return resultList;
        }

        /**
         * Метод, скачивающий из базы данных логины всех пользователей.
         * @param dataSnapshot Снимок базы данных.
         * @return Логины всех пользователей.
         */
        public static ArrayList<String> getAllLogins(DataSnapshot dataSnapshot) {
            ArrayList<String> resultList = getUnverifiedLogins(dataSnapshot);
            resultList.addAll(getVerifiedLogins(dataSnapshot));
            return resultList;
        }

        /**
         * Метод, скачивающий из базы данных все заявки, ответственным за которые является определенный работник.
         * @param dataSnapshot Снимок базы данных.
         * @param overseerLogin Логин ответственного, заявки которого нужно получить.
         * @return Заявки определенного ответственного.
         */
        public static ArrayList<Ticket> getOverseerTicketList(DataSnapshot dataSnapshot, String overseerLogin){
            ArrayList<Ticket> resultList = new ArrayList<Ticket>();
            for (DataSnapshot ticketRecord : dataSnapshot.child(DatabaseVariables.Tickets.DATABASE_MARKED_TICKET_TABLE).getChildren()) {
                Ticket ticket = ticketRecord.getValue(Ticket.class);
                if (ticket.getAdminId().equals(overseerLogin))
                    resultList.add(ticket);
            }
            return resultList;
        }

        /**
         * Метод, скачивающий из базы данных заявки определнного пользователя, находящиеся в определенном состоянии.
         * @param dataSnapshot Снимок базы данных.
         * @param databaseTablePath Путь в базе данных к необходимой категории заявок.
         * @param userLogin Логин пользователя.
         * @return Заявки определенного пользователя, находящиеся в определенном состоянии.
         */
        public static ArrayList<Ticket> getUserSpecificTickets(DataSnapshot dataSnapshot, String databaseTablePath, String userLogin){
            ArrayList<Ticket> resultList = new ArrayList<Ticket>();
            for (DataSnapshot ticketRecord : dataSnapshot.child(databaseTablePath).getChildren()) {
                Ticket markedTicket = ticketRecord.getValue(Ticket.class);
                if (markedTicket.getUserId().equals(userLogin))
                    resultList.add(markedTicket);
            }
            return resultList;
        }

        /**
         *  Метод, скачивающий из базы данных все заявки, находящиеся в определенном состоянии.
         * @param dataSnapshot Снимок базы данных.
         * @param databaseTablePath Путь в базе данных к необходимой категории заявок.
         * @return Заявки, находящиеся в определенном состоянии.
         */
        public static ArrayList<Ticket> getSpecificTickets(DataSnapshot dataSnapshot, String databaseTablePath){
            ArrayList<Ticket> resultList = new ArrayList<Ticket>();
            for (DataSnapshot ticketRecord : dataSnapshot.child(databaseTablePath).getChildren())
                resultList.add(ticketRecord.getValue(Ticket.class));
            return resultList;
        }

        public static ArrayList<Ticket> getAllTickets(DataSnapshot dataSnapshot){
            ArrayList<Ticket> resultList = getSpecificTickets(dataSnapshot, DatabaseVariables.Tickets.DATABASE_MARKED_TICKET_TABLE);
            resultList.addAll(getSpecificTickets(dataSnapshot, DatabaseVariables.Tickets.DATABASE_UNMARKED_TICKET_TABLE));
            resultList.addAll(getSpecificTickets(dataSnapshot, DatabaseVariables.Tickets.DATABASE_SOLVED_TICKET_TABLE));
            return resultList;
        }

    }

}
