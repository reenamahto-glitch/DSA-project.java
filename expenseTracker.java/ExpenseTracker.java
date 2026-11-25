import java.io.*;
import java.util.Scanner;
import java.util.Stack;

public class ExpenseTracker {

    // ---------- Node of our own linked list ----------
    static class Node {
        String id;
        String category;
        double amount;
        Node next;

        Node(String id, String category, double amount) {
            this.id = id;
            this.category = category;
            this.amount = amount;
        }
    }

    // ---------- Own Linked List ----------
    static class ExpenseList {
        Node head;
        int size = 0;

        void addLast(Node n) {
            if (head == null) {
                head = n;
            } else {
                Node t = head;
                while (t.next != null) t = t.next;
                t.next = n;
            }
            size++;
        }

        boolean removeById(String id) {
            if (head == null) return false;
            if (head.id.equals(id)) {
                head = head.next;
                size--;
                return true;
            }
            Node t = head;
            while (t.next != null) {
                if (t.next.id.equals(id)) {
                    t.next = t.next.next;
                    size--;
                    return true;
                }
                t = t.next;
            }
            return false;
        }

        // Bubble sort by amount (low to high), stable
        void sortByAmount() {
            if (head == null || head.next == null) return;
            boolean swapped;
            do {
                swapped = false;
                Node t = head;
                while (t.next != null) {
                    if (t.amount > t.next.amount) {
                        swapData(t, t.next);
                        swapped = true;
                    }
                    t = t.next;
                }
            } while (swapped);
        }

        private void swapData(Node a, Node b) {
            String i = a.id; a.id = b.id; b.id = i;
            String c = a.category; a.category = b.category; b.category = c;
            double m = a.amount; a.amount = b.amount; b.amount = m;
        }

        Node getAt(int index) {
            Node t = head;
            for (int i = 0; i < index; i++) t = t.next;
            return t;
        }
    }

    // ---------- Globals ----------
    static ExpenseList list = new ExpenseList();
    static Stack<String> undoStack = new Stack<>();   // stores added IDs
    static int nextId = 101;
    static final String FILE_NAME = "expenses.txt";
    static Scanner sc = new Scanner(System.in);

    // ---------- Helpers ----------
    static String fmt(double a) {
        if (a == Math.floor(a)) return String.valueOf((long) a);
        return String.valueOf(a);
    }

    static String line(Node n) {
        return n.id + " | " + n.category + " | " + fmt(n.amount);
    }

    // ---------- File Handling ----------
    static void loadFromFile() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String s;
            while ((s = br.readLine()) != null) {
                s = s.trim();
                if (s.isEmpty()) continue;
                String[] p = s.split(",");
                if (p.length != 3) continue;
                String id = p[0].trim();
                String cat = p[1].trim();
                double amt = Double.parseDouble(p[2].trim());
                list.addLast(new Node(id, cat, amt));
                undoStack.push(id);
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num >= nextId) nextId = num + 1;
                } catch (Exception ignored) { }
            }
        } catch (Exception e) {
            System.out.println("Could not read file: " + e.getMessage());
        }
    }

    static void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            Node t = list.head;
            while (t != null) {
                pw.println(t.id + "," + t.category + "," + fmt(t.amount));
                t = t.next;
            }
        } catch (IOException e) {
            System.out.println("Could not save file: " + e.getMessage());
        }
    }

    // ---------- Features (Methods) ----------
    static void addExpense() {
        System.out.print("Category: ");
        String cat = sc.nextLine().trim();
        if (cat.isEmpty()) {
            System.out.println("Invalid category.");
            return;
        }
        System.out.print("Amount: ");
        double amt;
        try {
            amt = Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Amount must be greater than 0.");
            return;
        }
        if (Double.isNaN(amt) || Double.isInfinite(amt) || amt <= 0) {
            System.out.println("Invalid amount. Amount must be greater than 0.");
            return;
        }
        String id = "X" + nextId++;
        Node n = new Node(id, cat, amt);
        list.addLast(n);
        undoStack.push(id);
        System.out.println("Expense added. ID: " + line(n));
    }

    static void viewAll() {
        if (list.head == null) {
            System.out.println("No expenses found.");
            return;
        }
        int i = 1;
        Node t = list.head;
        while (t != null) {
            System.out.println(i++ + ". " + line(t));
            t = t.next;
        }
    }

    static void sortExpenses() {
        if (list.head == null) {
            System.out.println("No expenses found.");
            return;
        }
        list.sortByAmount();
        viewAll();
    }

    static void binarySearch() {
        if (list.head == null) {
            System.out.println("No expenses found.");
            return;
        }
        System.out.print("Enter amount to search: ");
        double key;
        try {
            key = Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }
        // Binary search works only on sorted data
        list.sortByAmount();

        int low = 0, high = list.size - 1, found = -1;
        while (low <= high) {
            int mid = (low + high) / 2;
            double midVal = list.getAt(mid).amount;
            if (midVal == key) {
                found = mid;
                high = mid - 1;      // go left to find first match
            } else if (midVal < key) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        if (found == -1) {
            System.out.println("Not found.");
        } else {
            // print all expenses having this amount
            int i = found;
            while (i < list.size && list.getAt(i).amount == key) {
                System.out.println("Found: " + line(list.getAt(i)));
                i++;
            }
        }
    }

    static void undoLast() {
        if (undoStack.isEmpty()) {
            System.out.println("Nothing to undo.");
            return;
        }
        String id = undoStack.pop();
        list.removeById(id);
        System.out.println("Last expense " + id + " removed.");
        System.out.println("Total expenses: " + list.size);
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        loadFromFile();

        while (true) {
            System.out.println("\n===== EXPENSE TRACKER =====");
            System.out.println("1. Add expense");
            System.out.println("2. View all expenses");
            System.out.println("3. Sort by amount (low to high)");
            System.out.println("4. Binary search by amount");
            System.out.println("5. Undo last expense");
            System.out.println("6. Exit");
            System.out.print("Choose option: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": addExpense(); break;
                case "2": viewAll(); break;
                case "3": sortExpenses(); break;
                case "4": binarySearch(); break;
                case "5": undoLast(); break;
                case "6":
                    saveToFile();
                    System.out.println("Data saved to " + FILE_NAME + ". Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}