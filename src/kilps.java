        import com.google.common.collect.ArrayListMultimap;
        import com.google.common.collect.Multimap;
        import com.stata.sfi.Macro;
        import com.stata.sfi.SFIToolkit;

        import javax.swing.*;
        import javax.swing.event.TreeSelectionEvent;
        import javax.swing.event.TreeSelectionListener;
        import javax.swing.table.DefaultTableModel;
        import javax.swing.tree.DefaultMutableTreeNode;
        import javax.swing.tree.TreePath;
        import javax.swing.tree.TreeSelectionModel;
        import java.awt.*;
        import java.awt.event.ActionEvent;
        import java.awt.event.ActionListener;
        import java.io.BufferedReader;
        import java.io.FileNotFoundException;
        import java.io.IOException;
        import java.nio.file.Files;
        import java.nio.file.Paths;
        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.Collection;
        import java.util.List;

// 파일을 읽어서 list
public class kilps {
    public static void main(String[] args) {
        String m_path="/Users/seti/Desktop/";
        view(m_path);
    }

    public static int db(String[] args){
        String Person_path= Macro.getGlobal("c(sysdir_personal)");
        view(Person_path);
        return 0;
    }

    public static void view(String args) {
        String file=args+"klips_level.csv";
        String file_lb=args+"klips_lb.csv";

        String parser=":";
        final List<List<String>> tree_infor = Resource_Input(file,parser);
        tree_infor.remove(0);
        List<List<String>> lb_infor = Resource_Input(file_lb,parser);
        lb_infor.remove(0);
        final String[] header={"변수","값라벨명","값","값설명"};

        // level1 array list -> 0과 일치하지 않는 경우에만 생성
        List<List<String>> level1_list=Level_Ext(tree_infor,2,0,0);

        // level2 array list -> 0과 일치하는 경우에만 생성
        List<List<String>> level2_list=Level_Ext(tree_infor,2,0,1);


        final Multimap<String,List<String>> lbmap=VlbMap(lb_infor,0);


        DefaultMutableTreeNode root = new DefaultMutableTreeNode("한국노동패널");
        DefaultMutableTreeNode hid = new DefaultMutableTreeNode("가구용");
        DefaultMutableTreeNode pid = new DefaultMutableTreeNode("개인용");
        final JTree tree = new JTree(root);
        root.add(hid);
        root.add(pid);

        DefaultMutableTreeNode[] level1_obj=new DefaultMutableTreeNode[level1_list.size()];
        for(int i=0;i<level1_list.size();i++) {
            List<String> val = level1_list.get(i);
            String lable=val.get(0);
            level1_obj[i]=new DefaultMutableTreeNode(lable);
            hid.add(level1_obj[i]);
        }

        for(int i=0;i<level2_list.size();i++) {
            List<String> val = level2_list.get(i);
            String lable=val.get(0);
            String varname=val.get(1);
            String vinfor=val.get(4);

            int level_val=Integer.valueOf(val.get(3))-1;
            DefaultMutableTreeNode level2_obj = null;
            level2_obj=new DefaultMutableTreeNode(new stata.gomsdb.VarInfo(lable,varname,vinfor));
            level1_obj[level_val].add(level2_obj);
        }

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        JScrollPane scroll_Tree = new JScrollPane(tree);


        final String[][] lb_null=null;
        DefaultTableModel value_label = new DefaultTableModel(lb_null,header);
        final JTable table_content = new JTable(value_label);
        JScrollPane scroll_lb = new JScrollPane(table_content);

        final JTextArea infotxt=new JTextArea();

        final String[] sel_header={"변수","변수설명"};
        String[][] null_table=null;

        JButton jbtn1 = new JButton("add ->");
        DefaultTableModel sel_model = new DefaultTableModel(null_table,sel_header);
        final JTable table_sel = new JTable(sel_model);
        JScrollPane scroll_sel_content = new JScrollPane(table_sel);
        //  add 버튼 action
        ActionListener listener1=new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<List<String>> sel_array=new ArrayList<List<String>>();
                TreePath[] treepath=tree.getSelectionPaths();
                for(int i = 0; i<tree.getSelectionCount();i++) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                            treepath[i].getLastPathComponent();
                    Object nodeInfo = node.getUserObject();
                    stata.gomsdb.VarInfo var=(stata.gomsdb.VarInfo)nodeInfo;
                    String lb=var.toString();
                    String var_name=var.tovarString();
                    List<String> add_array=new ArrayList<String>();
                    add_array.add(var_name);
                    add_array.add(lb);
                    // System.out.println(add_array);
                    sel_array.add(add_array);
                }
                String[][] sel=List2Array(sel_array);

                int table_rows=table_sel.getRowCount();
                if(table_rows==0) {
                    DefaultTableModel sel_model = new DefaultTableModel(sel,sel_header);
                    table_sel.setModel(sel_model);
                }
                else {
                    int old_rows = table_sel.getRowCount();
                    int cols=sel_header.length;
                    String[][] old_sel_table =new String[old_rows][cols];
                    for(int i=0;i<old_rows;i++) {
                        for(int j=0;j<cols;j++) {
                            Object value=table_sel.getValueAt(i, j);
                            old_sel_table[i][j]=value.toString();
                            System.out.println(value.toString());
                        }
                    }
                    String[][] new_select_table = new String[old_sel_table.length+sel.length][];
                    System.arraycopy(old_sel_table,0,new_select_table,0,old_sel_table.length);
                    System.arraycopy(sel,0,new_select_table,old_sel_table.length,sel.length);

                    // model change
                    DefaultTableModel sel_model = new DefaultTableModel(new_select_table,sel_header);
                    table_sel.setModel(sel_model);

                }
            }};
        jbtn1.addActionListener(listener1);

        JButton jbtn2 = new JButton("remove <-");
        ActionListener listener2=new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = table_sel.getSelectedRows();
                DefaultTableModel tm = (DefaultTableModel) table_sel.getModel();
                for (int i = rows.length-1; i >= 0; i--) {
                    tm.removeRow(rows[i]);
                }
            }
        };
        jbtn2.addActionListener(listener2);

        JButton jbtn3 = new JButton("reset");
        ActionListener listener3=new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel reset_model = (DefaultTableModel) table_sel.getModel();
                reset_model.setRowCount(0);
            }
        };
        jbtn3.addActionListener(listener3);

        // Stata로 변수 정보를 보내는 버튼 및 이벤트
        final JTextField sendwave=new JTextField();
        sendwave.setPreferredSize(new Dimension(300, 30));

        final JTextField find_txt=new JTextField();
        find_txt.setPreferredSize(new Dimension(300, 30));

        JButton jbtn4 = new JButton("send");
        ActionListener listener4=new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int send_rows = table_sel.getRowCount();
                String[] send_table =new String[send_rows];
                String cmd_el="";
                for(int i=0;i<send_rows;i++) {
                    Object value=table_sel.getValueAt(i, 0);
                    send_table[i]=value.toString();
                    cmd_el=cmd_el+" "+value.toString();
                    // System.out.println(cmd_el);
                }
                String contents = sendwave.getText();
                Macro.setLocal("gomslist", cmd_el);
                Macro.setLocal("wave", contents);
                // System.out.println(contents);
                SFIToolkit.executeCommand("goms_use `gomslist', wave(`wave')", true);
            }
        };
        jbtn4.addActionListener(listener4);

        JPanel button = new JPanel();
        jbtn1.setPreferredSize(jbtn2.getPreferredSize());
        GridBagLayout grid_bt = new GridBagLayout();
        button.setLayout(grid_bt);
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=0;
        button.add(jbtn1,gbc);
        gbc.gridx=0;
        gbc.gridy=1;
        button.add(jbtn2,gbc);


        JSplitPane splitPane_info = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane_info.setTopComponent(scroll_lb);
        splitPane_info.setBottomComponent(infotxt);

        JPanel button_send_reset = new JPanel();
        jbtn4.setPreferredSize(jbtn3.getPreferredSize());
        button_send_reset.setLayout(new FlowLayout());
        button_send_reset.add(jbtn3);
        button_send_reset.add(jbtn4);

        JFrame main_dialog = new JFrame();
        GridBagLayout main_ly = new GridBagLayout();
        main_dialog.setLayout(main_ly);

        //scroll_Tree button scroll_sel_content splitPane_info button_send_reset

        // scroll_Tree.setBorder(BorderFactory.createEmptyBorder(10 , 10 , 10 , 10));
        // scroll_sel_content.setBorder(BorderFactory.createEmptyBorder(10 , 10 , 10 , 10));
        JLabel sendlabel=new JLabel("차수설정: [예1) 11~13차 설정 11 12 13 or 11/13] ");
        JLabel emplabel=new JLabel("(아무것도 입력하지 않으면 전체 차수설정; min=11, max 18)");
        JLabel findlabel=new JLabel("찾기");


        JPanel wavepanel=new JPanel();
        GridBagLayout wave_ly = new GridBagLayout();
        wavepanel.setLayout(wave_ly);
        gbinsert2(wavepanel,sendlabel,wave_ly,0,0,3,3);
        gbinsert2(wavepanel,emplabel,wave_ly,0,1,3,3);
        gbinsert2(wavepanel,sendwave,wave_ly,0,2,3,3);

        JPanel findpanel=new JPanel();
        findpanel.setLayout(wave_ly);
        gbinsert2(findpanel,findlabel,wave_ly,0,0,3,3);
        gbinsert2(findpanel,find_txt,wave_ly,0,2,3,3);




        gbinsert(main_dialog,scroll_Tree,main_ly,0,0,100,100);
        gbinsert(main_dialog,findpanel,main_ly,0,1,3,3);
        gbinsert(main_dialog,button,main_ly,1,0,5,10);
        gbinsert(main_dialog,scroll_sel_content,main_ly,2,0,50,100);
        gbinsert(main_dialog,wavepanel,main_ly,2,1,3,3);
        gbinsert(main_dialog,splitPane_info,main_ly,3,0,100,100);
        gbinsert(main_dialog,button_send_reset,main_ly,3,1,3,3);

        main_dialog.setTitle("고용조사 management system");
        main_dialog.setSize(1500,700);
        main_dialog.setVisible(true);



        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        tree.getLastSelectedPathComponent();
                boolean leaf=node.isLeaf();
                if(leaf==true) {
                    Object nodeInfo = node.getUserObject();
                    stata.gomsdb.VarInfo var=(stata.gomsdb.VarInfo)nodeInfo;
                    // System.out.println("selvar:"+var.tovarString()+":vinfor"+var.toinforString());
                    Collection<List<String>> values = lbmap.get(var.tovarString());
                    List<List<String>> yourList = new ArrayList<>(values);
                    String[][] val_lb_array=List2Array(yourList);
                    DefaultTableModel value_label = new DefaultTableModel(val_lb_array,header);
                    table_content.setModel(value_label);
                    infotxt.setText(var.toinforString());
                    infotxt.setEditable(false);
                }
                if(leaf==false) {
                    DefaultTableModel value_label = new DefaultTableModel(lb_null,header);
                    table_content.setModel(value_label);
                }
            }
        });
    }
    public static void gbinsert(JFrame a, Component c, GridBagLayout g,int x, int y, int w, int h){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill= GridBagConstraints.BOTH;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx= w;
        gbc.weighty = h;
        g.setConstraints(c,gbc);
        a.add(c);
    }
    public static void gbinsert2(JPanel a, Component c, GridBagLayout g,int x, int y, int w, int h){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill= GridBagConstraints.BOTH;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx= w;
        gbc.weighty = h;
        g.setConstraints(c,gbc);
        a.add(c);
    }



    public static String[][] List2Array(List<List<String>> inlist) {
        String[][] out_array=new String[inlist.size()][];
        for (int i = 0; i < inlist.size(); i++) {
            List<String> row = inlist.get(i);
            out_array[i] = row.toArray(new String[row.size()]);
        }
        return out_array;
    }

    public static Object[] obj_array(List<List<String>> master , int mcol , List<List<String>> using, int ucol) {
        Object[] out_array=new Object[master.size()];
        List<List<String>> using_temp=using;
        for(int i=0; i<master.size(); i++) {
            List<String> value_list=master.get(i);
            String varname=String.valueOf(value_list.get(mcol));
            String[][] result_array=find_var(using_temp,varname,ucol);
            out_array[i]=result_array;
        }
        return (Object[]) out_array;
    }

    public static String[][] find_var(List<List<String>> inlist, String keyword, int col) {
        List<List<String>> templist = new ArrayList<List<String>>();;

        for (int i = 0; i < inlist.size(); i++) {
            List<String> varname_temp = inlist.get(i);
            String varname=varname_temp.get(col);
            boolean key_var=keyword.equalsIgnoreCase(varname);
            // System.out.println("varname:"+varname+",keyword:"+keyword+"t:"+key_var);
            if(key_var) {
                templist.add(varname_temp);
            }
        }
        // AlistPrint(templist);
        String[][] out_array=List2Array(templist);
        return out_array;
    }


    public static List<List<String>> Level_Ext(List<List<String>> inlist, int col, int value, int equal_value) {
        List<List<String>> outlist = new ArrayList<List<String>>();
        int l;
        l=inlist.size();
        for(int i=0; i<l; i++) {
            List<String> val = inlist.get(i);
            // System.out.println(val);
            String level=val.get(col);
            // System.out.println(level);
            int level_value=Integer.valueOf(level);
            if(equal_value==1) {
                if(level_value==value) {
                    outlist.add(val);
                }
            }
            if(equal_value==0) {
                if(level_value!=value) {
                    outlist.add(val);
                }
            }
        }
        return outlist;
    }


    public static void AlistPrint(List<List<String>> inlist) {
        for(int i=0; i<inlist.size(); i++)
        {
            System.out.println(inlist.get(i));
        }
    }

    // Arraylist를 받아 map 생성
    public static Multimap <String,List<String>>VlbMap(List<List<String>> inlist, int keycol) {
        Multimap <String,List<String>> returnMap = ArrayListMultimap.create();
        int l;
        l=inlist.size();
        // System.out.println(l);
        for(int i=0; i<l; i++) {
            List<String> val = inlist.get(i);
            String key=String.valueOf(val.get(keycol));
            returnMap.put(key, val);
        }
        return returnMap;
    }


    public static List<List<String>> Resource_Input(String file, String parser) {
        List<List<String>> ret = new ArrayList<List<String>>();
        BufferedReader br = null;
        try{
            br = Files.newBufferedReader(Paths.get(file));
            //Charset.forName("UTF-8");
            String line = "";

            while((line = br.readLine()) != null){
                //CSV 1행을 저장하는 리스트
                List<String> tmpList = new ArrayList<String>();
                String array[] = line.split(parser);
                //배열에서 리스트 반환
                tmpList = Arrays.asList(array);
                // System.out.println(tmpList);
                ret.add(tmpList);

            }
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        } finally{
            try{
                if(br != null){
                    br.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return(ret);
    }
    static public class VarInfo {
        public String label;
        public String varname;
        public String var_des;

        public VarInfo(String des, String var, String infor) {
            label = des;
            varname = var;
            var_des=infor;
        }
        public String tolbString() {
            return label;
        }
        public String tovarString() {
            return varname;
        }
        public String toinforString() {
            return var_des;
        }
        public String toString() {
            return label;
        }
    }
}


