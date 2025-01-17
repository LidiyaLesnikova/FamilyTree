package com.example.FamilyTree.View;

import com.example.FamilyTree.Presenter.Presenter;
import com.example.FamilyTree.View.Commands.MainMenu;
import com.example.FamilyTree.View.Commands.MenuInterface;
import com.example.FamilyTree.View.Printable.Printable;
import com.example.FamilyTree.View.Printable.printObjectList;

import java.util.*;


public class Console implements View {
    private Scanner scan;
    private Presenter presenter;
    private MenuInterface mainMenu;
    private Printable printable;
    private boolean work;
    private int objectSelection;

    public Console() {
        printable = new printObjectList();
        presenter = new Presenter(printable);
        scan = new Scanner(System.in);
        work = true;
    }

    @Override
    public void objectSelection() {
        System.out.println("Выберите объект для семейного дерева: ");
        List<String> objectList = presenter.getObjectList();
        System.out.println(toPrint(objectList));
        objectSelection = inputNumMenu(1, objectList.size())-1;

        boolean foolMenu = true;
        if (presenter.runFamilyTree(objectSelection)) {
            System.out.println(takeSortingFamilyTree());
        } else {
            System.out.println("Семейное дерево " + objectList.get(objectSelection) + " пустое");
            foolMenu = false;
        }
        mainMenu = new MainMenu(this, foolMenu);
        menuAction();
    }

    private String toPrint(List<String> objectList) {
        StringBuilder stringBuilder = new StringBuilder();
        int index = 1;
        for (String list : objectList) {
            stringBuilder.append(index++ + ": ");
            stringBuilder.append(list);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    private String takeSortingFamilyTree() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Общий список лиц семьи: " + printFamilyTree(-1,-1));
        presenter.familyTreeSortByBirthday();
        stringBuilder.append("\n\nСортировка по дню рождения: " + printFamilyTree(-1,-1));
        presenter.familyTreeSortByName();
        stringBuilder.append("\n\nСортировка по имени: "+ printFamilyTree(-1, -1));
        return stringBuilder.toString();
    }

    private void menuAction() {
        work = true;
        while (work) {
            System.out.println("\nВыберите пункт меню: ");
            System.out.println(mainMenu.printMenu());
            int choice = inputNumMenu(1,mainMenu.size());
                mainMenu.execute(choice);
        }
    }

    public int chooseObjectFamilyTree(){
        System.out.println("Выберите объект из семейного дерева: ");
        System.out.println(printFamilyTree(-1, -1));
        return inputNumMenu(1, presenter.takeSizeFamilyTree(-1, -1));
    }

    public String printFamilyTree(int whom, int gendInd) {
        return presenter.printFamilyTree(whom, gendInd);
    }

    public void addRecord() {
        Map<String, Object> familyObject = fillObject();
        int numObject = presenter.addFamilyTree(familyObject, objectSelection);
        getParents(numObject);
        getChildren(numObject);
        work = true;
    }

    public void modifyRecord() {
        int whom = chooseObjectFamilyTree();

        System.out.println("Выберите изменяемый реквизит объекта: ");
        List fields = presenter.getListFields();
        for (int i = 0; i < fields.size(); i++) {
            System.out.printf("%d - %s\n",i+1,fields.get(i));
        }
        int numField = inputNumMenu(1,fields.size());
        switch (numField){
            case 1:
                String newName = getName("изменить имя: ");
                presenter.updateObjectName(whom-1, newName);
                System.out.println("Имя изменено");
                break;
            case 2:
                String newBirthday = getBirthday("изменить дату рождения (формат гггг-мм-дд):");
                presenter.updateObjectBirthday(whom-1, newBirthday);
                System.out.println("Дата рождения изменена");
                break;
            case 3:
                int newGender = getGender("изменить пол: \n");
                presenter.updateObjectGender(whom-1, newGender);
                System.out.println("Пол изменен");
                break;
            case 4:
                getParents(whom-1);
                System.out.println("Внесены данные о родителях");
                break;
            case 5:
                getChildren(whom-1);
                System.out.println("Внесены данные о детях");
                break;
        }
        work = true;
    }

    private void getParents(int whom) {
        System.out.println("Ввести/выбрать мать объекта?  1-Да / 0-Нет: ");
        if (bool()) {
            addParents(whom, 1);
        }
        System.out.println("Ввести отца объекта?  1-Да / 0-Нет: ");
        if (bool()) {
            addParents(whom, 0);
        }
    }

    private void getChildren(int whom){
        work = true;
        while (work) {
            System.out.println("Ввести детей объекта?  1-Да / 0-Нет: ");
            if (bool()) {
                System.out.println("Выберите из списка:");
                System.out.println(printFamilyTree(whom, -1));
                int sizeFamilyTree = presenter.takeSizeFamilyTree(whom, -1);
                System.out.println((sizeFamilyTree+1)+". ДОБАВИТЬ ОБЪЕКТ");
                int childInd = inputNumMenu(1, sizeFamilyTree+1);
                if (childInd==sizeFamilyTree+1){
                    Map<String, Object> familyObject = fillObject();
                    familyObject.put("parent"+familyObject.get("gender"),whom);
                    int newObject = presenter.addFamilyTree(familyObject, objectSelection);
                }
                presenter.updateObjectChildren(whom, childInd-1, -1, true);
            } else {
                work = false;
            }
        }
    }

    private void addParents(int whom, int gendInd){
        System.out.println("Выберите из списка:");
        System.out.println(printFamilyTree(whom, gendInd));
        int sizeFamilyTree = presenter.takeSizeFamilyTree(whom, gendInd);
        System.out.println((sizeFamilyTree+1)+". ДОБАВИТЬ ОБЪЕКТ");
        int parentsInd = inputNumMenu(1, sizeFamilyTree+1);
        if (parentsInd==sizeFamilyTree+1){
            Map<String, Object> familyObject = fillObject();
            familyObject.put("child"+gendInd,whom);
            int numObject = presenter.addFamilyTree(familyObject, objectSelection);
        }
        presenter.updateObjectParents(whom, parentsInd-1, gendInd, true);
    }

    private int inputNumMenu(int from, int to) {
        work = true;
        String line = "";
        while (work) {
            line = scan.nextLine();
            if (!checkLine(line, from, to)) {
                System.out.println("Ошибка ввода, еще раз");
                continue;
            }
            work = false;
        }
        return Integer.parseInt(line);
    }

    private boolean checkLine(String line, int from, int to) {
        if (!line.matches("[0-9]+")){
            return false;
        }
        int choice = Integer.parseInt(line);
        return choice >= from && choice <= to;
    }

    private Map<String, Object> fillObject() {
        Map<String, Object> parameters = new HashMap();
        String name = getName("имя: ");
        String birthday = getBirthday("дата рождения формата гггг-мм-дд: ");
        int gender = getGender("пол: \n");
        parameters.put("name", name);
        parameters.put("birthday", birthday);
        parameters.put("gender", gender);
        return parameters;
    }

    private String getName(String str) {
        System.out.println(str);
        return scan.nextLine();
    }

    private String getBirthday(String str) {
        String birthday = "";
        work = true;
        while (work) {
            System.out.println(str);
            birthday = scan.nextLine();
            if (!birthday.matches("[1-2][0-9][0-9][0-9][-](0[0-9]|1[0-2])[-](0[0-9]|1[0-9]|2[0-9]|3[0-1])")) {
                System.out.println("Ошибка ввода, еще раз");
                continue;
            }
            work = false;
        }
        return birthday;
    }

    private int getGender(String str) {
        List genderList = presenter.getGenderList();
        System.out.println(str+printGender(genderList));
        return inputNumMenu(1, genderList.size())-1;
    }

    private String printGender(List genderList) {
        StringBuilder stringBuilder = new StringBuilder();
        int index = 1;
        for (int i = 0; i < genderList.size(); i++) {
            stringBuilder.append(index++ + ": ");
            stringBuilder.append(genderList.get(i));
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public boolean bool() {
        int choice = inputNumMenu(0,1);
        return choice != 0;
    }

    public void deleteRecord() {
        System.out.println(presenter.deleteRecordFamilyTree(chooseObjectFamilyTree()));
        System.out.println(printFamilyTree(-1,-1));
        work = true;
    }

    public void viewRecord() {
        System.out.println(presenter.showFamilyTree(chooseObjectFamilyTree()));
        work = true;
    }

    public void showRelatives() {
        System.out.println("Выберите первый объект: ");
        System.out.println(printFamilyTree(-1,-1));
        int sizeFamilyTree = presenter.takeSizeFamilyTree(-1,-1);
        int object1 = inputNumMenu(1,sizeFamilyTree);
        System.out.println("Выберите второй объект: ");
        int object2 = inputNumMenu(1,sizeFamilyTree);
        System.out.println(presenter.showRelatives(object1-1,object2-1));
        work = true;
    }

    public void quit() {
        System.out.println("До свидания)");
        work = false;
    }
}