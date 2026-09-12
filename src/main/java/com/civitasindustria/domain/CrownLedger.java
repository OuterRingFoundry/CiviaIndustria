package com.civitasindustria.domain;

import java.io.*;
import java.util.*;

/** Single currency. Every issued crown is backed by a deposited diamond. */
public final class CrownLedger {
    public static final int VERSION=1, RATE=100, MAX_ACCOUNTS=10000, MAX_BYTES=400000;
    public static final long LIMIT=1_000_000_000L;
    public static final String[] GOODS={"minecraft:iron_ingot","minecraft:copper_ingot","minecraft:redstone","minecraft:andesite"};
    private static final int[] BASE={8,4,2,1};
    private final Map<UUID,Long> balances=new HashMap<>();
    private final long[] stock=new long[GOODS.length];
    private long reserve,issued,treasury,lastDay=-1;
    private int index=10000,imbalance;
    public long balance(UUID id){return balances.getOrDefault(id,0L);}
    public long reserve(){return reserve;}
    public long issued(){return issued;}
    public long treasury(){return treasury;}
    public long stock(int good){checkGood(good);return stock[good];}
    public int index(){return index;}
    public int ask(int good){checkGood(good);return Math.max(2,(BASE[good]*index*120+999999)/1000000);}
    public int bid(int good){checkGood(good);return Math.max(1,BASE[good]*index*80/1000000);}
    private static void checkGood(int good){if(good<0||good>=GOODS.length)throw new IllegalArgumentException("Unknown good");}
    private boolean canCredit(UUID id,long value){return value>0&&balance(id)<=LIMIT-value&&(balances.containsKey(id)||balances.size()<MAX_ACCOUNTS);}
    private void credit(UUID id,long value){balances.put(id,balance(id)+value);}
    private void debit(UUID id,long value){long next=balance(id)-value;if(next==0)balances.remove(id);else balances.put(id,next);}
    public boolean deposit(UUID id){if(issued>LIMIT-RATE||!canCredit(id,RATE))return false;reserve++;issued+=RATE;credit(id,RATE);return true;}
    public boolean redeem(UUID id){if(reserve==0||balance(id)<RATE)return false;debit(id,RATE);reserve--;issued-=RATE;return true;}
    public boolean transfer(UUID from,UUID to,long amount){if(from.equals(to)||amount<=0||balance(from)<amount||!canCredit(to,amount))return false;debit(from,amount);credit(to,amount);return true;}
    /** Voluntary market funding is a transfer, never an issuance. */
    public boolean fund(UUID id){if(balance(id)<RATE||treasury>LIMIT-RATE)return false;debit(id,RATE);treasury+=RATE;return true;}
    public boolean buy(UUID id,int good){checkGood(good);int price=ask(good);if(stock[good]==0||balance(id)<price)return false;debit(id,price);treasury+=price;stock[good]--;imbalance=Math.min(200,imbalance+1);return true;}
    public boolean sell(UUID id,int good){checkGood(good);int price=bid(good);if(treasury<price||stock[good]>=LIMIT||!canCredit(id,price))return false;treasury-=price;credit(id,price);stock[good]++;imbalance=Math.max(-200,imbalance-1);return true;}
    /** One bounded step at a new observed day. No replay of absent days. */
    public boolean day(long day,long seed){if(day<0||day==lastDay)return false;if(lastDay<0){lastDay=day;return true;}lastDay=day;long hash=seed^(day*0x9E3779B97F4A7C15L);hash=(hash^(hash>>>30))*0xBF58476D1CE4E5B9L;int jitter=(int)Math.floorMod(hash,101L)-50;int pressure=Math.max(-200,Math.min(200,imbalance+jitter));index=Math.max(5000,Math.min(20000,index+(index*pressure)/10000));imbalance=0;return true;}
    public byte[] encode(){try{var bytes=new ByteArrayOutputStream();var out=new DataOutputStream(bytes);out.writeInt(VERSION);out.writeLong(reserve);out.writeLong(issued);out.writeLong(treasury);out.writeLong(lastDay);out.writeInt(index);out.writeInt(imbalance);out.writeInt(balances.size());for(UUID id:new TreeSet<>(balances.keySet())){out.writeLong(id.getMostSignificantBits());out.writeLong(id.getLeastSignificantBits());out.writeLong(balance(id));}for(long count:stock)out.writeLong(count);out.flush();return bytes.toByteArray();}catch(IOException e){throw new IllegalStateException(e);}}
    public static CrownLedger decode(byte[] bytes){if(bytes.length>MAX_BYTES)throw new IllegalArgumentException("Ledger byte limit");try{var in=new DataInputStream(new ByteArrayInputStream(bytes));if(in.readInt()!=VERSION)throw new IOException("Ledger version");var l=new CrownLedger();l.reserve=in.readLong();l.issued=in.readLong();l.treasury=in.readLong();l.lastDay=in.readLong();l.index=in.readInt();l.imbalance=in.readInt();int count=in.readInt();if(count<0||count>MAX_ACCOUNTS||l.reserve<0||l.reserve>LIMIT/RATE||l.issued!=l.reserve*RATE||l.issued>LIMIT||l.treasury<0||l.lastDay< -1||l.index<5000||l.index>20000||Math.abs((long)l.imbalance)>200)throw new IOException("Ledger envelope");long sum=l.treasury;for(int i=0;i<count;i++){UUID id=new UUID(in.readLong(),in.readLong());long value=in.readLong();if(value<=0||value>LIMIT||l.balances.put(id,value)!=null)throw new IOException("Account");sum=Math.addExact(sum,value);}for(int i=0;i<l.stock.length;i++){l.stock[i]=in.readLong();if(l.stock[i]<0||l.stock[i]>LIMIT)throw new IOException("Stock");}if(in.available()!=0||sum!=l.issued)throw new IOException("Currency conservation");return l;}catch(IOException|ArithmeticException e){throw new IllegalArgumentException("Refusing damaged ledger",e);}}
}
