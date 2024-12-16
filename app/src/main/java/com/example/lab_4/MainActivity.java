package com.example.lab_4;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TabHost;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> items;
    private ArrayList<String> favorites;
    private SQLiteDatabase db;
    private ListView favoritesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        loadItems(); // Загружаем основные элементы
        setupUI(); // Настраиваем интерфейс

        favoritesListView = findViewById(R.id.favoritesListView);
        registerForContextMenu(favoritesListView);
        loadFavorites(); // Загружаем избранные элементы
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupUI() {
        // Настройка вкладок
        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("MAIN");
        tabSpec.setContent(R.id.linearLayout);
        tabSpec.setIndicator("Главная страница");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Follows");
        tabSpec.setContent(R.id.linearLayout2);
        tabSpec.setIndicator("Избранное");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Map");
        tabSpec.setContent(R.id.linearLayout3);
        tabSpec.setIndicator("Карта");
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);

        WebView webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://yandex.by/maps/157/minsk/?ll=27.555691%2C53.902735&z=12");
    }

    private void loadItems() {
        items = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(DBHelper.COLUMN_NAME);
            do {
                items.add(cursor.getString(nameIndex));
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        ListView listView = findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        registerForContextMenu(listView); // Регистрация контекстного меню
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        if (v.getId() == android.R.id.list) { // Основной список
            menu.add(0, 1, 0, "В избранное");
            menu.add(0, 2, 1, "Удалить");
        } else if (v.getId() == R.id.favoritesListView) { // Список избранного
            menu.add(0, 3, 0, "Редактировать");
            menu.add(0, 4, 1, "Удалить");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (info != null) {
            if (item.getItemId() == 1) { // В избранное
                String selectedItem = items.get(info.position);
                ContentValues values = new ContentValues();
                values.put(DBHelper.COLUMN_NAME, selectedItem);
                db.insert(DBHelper.TABLE_FAVORITES, null, values);
                loadFavorites(); // Обновление списка избранного
                Toast.makeText(this, selectedItem + " добавлено в избранное", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == 2) { // Удалить из основного списка
                String selectedItem = items.get(info.position);
                db.delete(DBHelper.TABLE_NAME, DBHelper.COLUMN_NAME + " = ?", new String[]{selectedItem});
                loadItems(); // Обновление основного списка
                return true;
            } else if (item.getItemId() == 3) { // Редактировать в списке избранного
                String selectedItem = favorites.get(info.position).split("\n")[0]; // Получаем только имя
                showEditDialog(selectedItem); // Вызов диалога редактирования
                return true;
            } else if (item.getItemId() == 4) { // Удалить из списка избранного
                String selectedItem = favorites.get(info.position).split("\n")[0]; // Получаем только имя
                int rowsDeleted = db.delete(DBHelper.TABLE_FAVORITES, DBHelper.COLUMN_NAME + " = ?", new String[]{selectedItem});

                Log.d("Favorites", "Удаление: " + selectedItem + ", Rows deleted: " + rowsDeleted);

                if (rowsDeleted > 0) {
                    Toast.makeText(this, selectedItem + " удалено из избранного", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                }

                loadFavorites(); // Обновление списка избранного
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    private void loadFavorites() {
        favorites = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.TABLE_FAVORITES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(DBHelper.COLUMN_NAME);
            int descriptionIndex = cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPTION);
            do {
                String name = cursor.getString(nameIndex);
                String description = cursor.getString(descriptionIndex);
                Log.d("Favorites", "Name: " + name + ", Description: " + description); // Логирование
                favorites.add(name + "\n" + (description != null ? description : "Нет описания")); // Форматируем строку
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> favoritesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, favorites);
        favoritesListView.setAdapter(favoritesAdapter);
    }

    private void showEditDialog(String selectedItem) {
        // Создаем AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить описание к элементу");

        // Создаем EditText для ввода нового описания
        final EditText input = new EditText(this);
        builder.setView(input);

        // Получаем текущее описание элемента, если оно существует
        Cursor cursor = db.query(DBHelper.TABLE_FAVORITES, null, DBHelper.COLUMN_NAME + " = ?", new String[]{selectedItem}, null, null, null);
        String currentDescription = "";
        if (cursor.moveToFirst()) {
            int descriptionIndex = cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPTION);
            currentDescription = cursor.getString(descriptionIndex);
        }
        cursor.close();

        // Устанавливаем текущее описание в EditText
        input.setText(currentDescription);

        // Добавляем кнопки "Подтвердить" и "Отмена"
        builder.setPositiveButton("Подтвердить", (dialog, which) -> {
            String description = input.getText().toString();

            // Обновляем описание в базе данных
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_DESCRIPTION, description);

            // Создаем новую локальную переменную для курсора
            Cursor updateCursor = db.query(DBHelper.TABLE_FAVORITES, null, DBHelper.COLUMN_NAME + " = ?", new String[]{selectedItem}, null, null, null);
            if (updateCursor.moveToFirst()) {
                int idIndex = updateCursor.getColumnIndex(DBHelper.COLUMN_ID);
                int id = updateCursor.getInt(idIndex);
                db.update(DBHelper.TABLE_FAVORITES, values, DBHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            }
            updateCursor.close();

            // Обновляем список избранных
            loadFavorites();
            Toast.makeText(MainActivity.this, "Описание обновлено", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        // Создаем и отображаем диалог
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Вложенный класс DBHelper
    private static class DBHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "sqltest.db";
        private static final int DATABASE_VERSION = 2; // Увеличьте версию базы данных

        public static final String TABLE_NAME = "items";
        public static final String TABLE_FAVORITES = "favorites"; // Таблица для избранного
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description"; // Новая колонка для описания

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL);";
            db.execSQL(createTable);

            String createFavoritesTable = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " + // Существующая колонка
                    COLUMN_DESCRIPTION + " TEXT);"; // Новая колонка для описания
            db.execSQL(createFavoritesTable);

            // Добавляем данные в основную таблицу
            db.execSQL("INSERT INTO " + TABLE_NAME + " (" + COLUMN_NAME + ") VALUES " +
                    "('Уручье')," +
                    "('Борисовский тракт')," +
                    "('Восток')," +
                    "('Московская')," +
                    "('Парк Челюскинцев')," +
                    "('Академия наук')," +
                    "('Площадь Якуба Коласа')," +
                    "('Площадь Победы')," +
                    "('Октябрьская')," +
                    "('Площадь Ленина')," +
                    "('Институт Культуры')," +
                    "('Грушевка')," +
                    "('Михалово')," +
                    "('Петровщина')," +
                    "('Малиновка')," +
                    "('Каменная Горка')," +
                    "('Кунцевщина')," +
                    "('Спортивная')," +
                    "('Пушкинская')," +
                    "('Молодёжная')," +
                    "('Фрунзенская')," +
                    "('Немига')," +
                    "('Купаловская')," +
                    "('Первомайская')," +
                    "('Пролетарская')," +
                    "('Тракторный завод')," +
                    "('Партизанская')," +
                    "('Автозаводская')," +
                    "('Могилёвская');");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
            onCreate(db);
        }
    }
}