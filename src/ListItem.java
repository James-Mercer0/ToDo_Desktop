import java.io.*;

public class ListItem {
    String itemName;
    int itemNumber;
    int itemPriority;
    String itemInfo;
    static final String internalSeparator = "❒";
    static final String itemSeparator = "❂";

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
                String itemInfo = this.itemNumber + internalSeparator + this.itemPriority + internalSeparator + this.itemName + internalSeparator + this.itemInfo + itemSeparator;
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
            String itemInfo = this.itemNumber + internalSeparator + this.itemPriority + internalSeparator + this.itemName + internalSeparator + this.itemInfo + itemSeparator;
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

            int entries = 0;
            for (int i = 0; i < contents.length(); i++) {
                String currentChar = contents.charAt(i) + "";
                if (currentChar.equals(itemSeparator)) {
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
                if (currentChar.equals(itemSeparator)&& instances < listItemNum) {
                    instances++;
                    startingIndex = i+1;
                }
            }


            String postIndex = contents.substring(startingIndex);
            String itemInfo = postIndex.substring(0,postIndex.indexOf(itemSeparator));

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

            String currentListItem = contents.substring(0,contents.indexOf(itemSeparator));
            if(Integer.parseInt(String.valueOf(currentListItem.substring(0,currentListItem.indexOf(internalSeparator))))==listItemNum){
                String itemRemoved = fullList.replace(currentListItem+itemSeparator,"");
                try(BufferedWriter bw = new BufferedWriter(new FileWriter("./listStorage/List01.tdli"))) {
                bw.write(itemRemoved);
                }
            } else {
                for(int i=0;i<listItemNum;i++){
                    contents = contents.substring(contents.indexOf(itemSeparator) +1);
                    currentListItem = contents.substring(0,contents.indexOf(itemSeparator));
                    if(Integer.parseInt(String.valueOf(currentListItem.substring(0,currentListItem.indexOf(internalSeparator))))==listItemNum){
                        String itemRemoved = fullList.replace(currentListItem+itemSeparator,"");
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
                currentItem = contents.substring(0,contents.indexOf(itemSeparator)+1);
                String toBeReplaced = currentItem.substring(0,currentItem.indexOf(internalSeparator));
                currentItem = currentItem.replaceFirst(toBeReplaced,String.valueOf(entryNum));
                sb.append(currentItem);
                contents = contents.substring(contents.indexOf(itemSeparator)+1);
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

                currentItemInfo = remainingItems.substring(0, remainingItems.indexOf(itemSeparator));
                remainingItems = remainingItems.substring(remainingItems.indexOf(itemSeparator)+1);

                if (Integer.parseInt(currentItemInfo.substring(0,currentItemInfo.indexOf(internalSeparator)))==Integer.parseInt(itemInformation.substring(0,itemInformation.indexOf(internalSeparator)))) {
                    sb.append(itemInformation+itemSeparator);
                } else {
                    sb.append(currentItemInfo+itemSeparator);
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
