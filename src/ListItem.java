import java.io.*;

public class ListItem {
    String itemName;
    int itemNumber;
    int itemPriority;
    String itemInfo;

    public ListItem(int number, int priority, String name, String info) {
        this.itemNumber = number;
        this.itemPriority = priority;
        this.itemName = name;
        this.itemInfo = info;
    }

    public String getItemName() {
        return this.itemName;
    }

    public int getItemNumber() {
        return this.itemNumber;
    }

    public int getItemPriority() {
        return this.itemPriority;
    }

    public String getItemInfo() {
        return this.itemInfo;
    }

    File dir = new File("./listStorage");
    File savedList = new File("./listStorage/List01.tdli");
    String contents;

    public void saveListItem() {
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Dir created");
        }

        if (!savedList.exists()) {
            try {
                savedList.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader("./listStorage/List01.tdli"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            contents = sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e + "\nUnable to Read from File!");
        }

        if (contents.isEmpty()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("./listStorage/List01.tdli"))) {
                String itemInfo = this.itemNumber + "," + this.itemPriority + "," + this.itemName + "," + this.itemInfo + "|";
                bw.write(itemInfo);
                return;
            } catch (IOException e) {
                throw new RuntimeException(e + "\nUnable to write to File!");
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader("./listStorage/List01.tdli"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String itemInfo = this.itemNumber + "," + this.itemPriority + "," + this.itemName + "," + this.itemInfo + "|";
            if (sb.toString().contains(itemInfo)) {
                return;
            }
            sb.append(itemInfo);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter("./listStorage/List01.tdli"))) {
                bw.write(sb.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static int numOfListItems() {
        try (BufferedReader br = new BufferedReader(new FileReader("./listStorage/List01.tdli"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String contents = sb.toString();

//            String firstNum = contents.substring(0, contents.indexOf(","));
            int entries = 0;
            for (int i = 0; i < contents.length(); i++) {
                String currentChar = contents.charAt(i) + "";
                if (currentChar.equals("|")) {
                    entries++;
                }
            }
            return entries;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getListItemInfo(int listItemNum) {
        try (BufferedReader br = new BufferedReader(new FileReader("./listStorage/List01.tdli"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine())!=null){
                sb.append(line);
            }
            String contents = sb.toString();

            int startingIndex = 0;

            int instances = 0;

            for (int i = 0; i < contents.length(); i++) {
                String currentChar = contents.charAt(i)+"";
                if (currentChar.equals("|")&& instances < listItemNum) {
                    instances++;
                    startingIndex = i+1;
                }
            }


            String postIndex = contents.substring(startingIndex);
            String itemInfo = postIndex.substring(0,postIndex.indexOf("|"));

            return itemInfo;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteItem(int listItemNum){
        try (BufferedReader br = new BufferedReader(new FileReader("./listStorage/List01.tdli"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine())!=null){
                sb.append(line);
            }
            String fullList = sb.toString();
            String contents = sb.toString();

            String currentListItem = contents.substring(0,contents.indexOf("|"));
            if(Integer.parseInt(String.valueOf(currentListItem.substring(0,currentListItem.indexOf(","))))==listItemNum){
                String itemRemoved = fullList.replace(currentListItem+"|","");
                try(BufferedWriter bw = new BufferedWriter(new FileWriter("./listStorage/List01.tdli"))) {
                bw.write(itemRemoved);
                }
            } else {
                for(int i=0;i<listItemNum;i++){
                    contents = contents.substring(contents.indexOf("|") +1);
                    currentListItem = contents.substring(0,contents.indexOf("|"));
                    if(Integer.parseInt(String.valueOf(currentListItem.substring(0,currentListItem.indexOf(","))))==listItemNum){
                        String itemRemoved = fullList.replace(currentListItem+"|","");
                        try(BufferedWriter bw = new BufferedWriter(new FileWriter("./listStorage/List01.tdli"))) {
                            bw.write(itemRemoved);
                        }
                        return;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateListItems(){
        try (BufferedReader br = new BufferedReader(new FileReader("./listStorage/List01.tdli"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String updatedList;
            String contents = sb.toString();
            String currentItem;

            sb.replace(0,sb.length(),"");

            int entryNum = 0;
            for(int i=0;i<numOfListItems();i++){
                entryNum++;
                currentItem = contents.substring(0,contents.indexOf("|")+1);
                String toBeReplaced = currentItem.substring(0,currentItem.indexOf(","));
                currentItem = currentItem.replaceFirst(toBeReplaced,String.valueOf(entryNum));
                sb.append(currentItem);
                contents = contents.substring(contents.indexOf("|")+1);
            }
            updatedList = sb.toString();

            try(BufferedWriter bw = new BufferedWriter(new FileWriter("./listStorage/List01.tdli"))){
                bw.write(updatedList);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveUpdatedListItem(String itemInformation){
        try (BufferedReader br = new BufferedReader(new FileReader("./listStorage/List01.tdli"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String contents = sb.toString();
            sb.replace(0,sb.length(),"");

            String remainingItems = contents;
            String currentItemInfo;

            for(int i=0;i<numOfListItems();i++) {

                currentItemInfo = remainingItems.substring(0, remainingItems.indexOf("|"));
                remainingItems = remainingItems.substring(remainingItems.indexOf("|")+1);

                if (Integer.parseInt(currentItemInfo.substring(0,currentItemInfo.indexOf(",")))==Integer.parseInt(itemInformation.substring(0,itemInformation.indexOf(",")))) {
                    sb.append(itemInformation+"|");
                } else {
                    sb.append(currentItemInfo+"|");
                }
            }

            try(BufferedWriter bw = new BufferedWriter(new FileWriter("./listStorage/List01.tdli"))){
                bw.write(sb.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
